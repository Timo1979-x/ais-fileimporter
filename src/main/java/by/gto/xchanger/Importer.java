package by.gto.xchanger;

import by.gto.erip.exceptions.EntityInvalidException;
import by.gto.erip.exceptions.MinimumAllowedDateViolationException;
import by.gto.erip.helpers.DateHelpers;
import by.gto.erip.model.ChecksV3;
import by.gto.library.entity.RegNumberParseResult;
import by.gto.library.helpers.BlankNumberHelpers;
import by.gto.library.helpers.GuidHelpers;
import by.gto.library.helpers.RegNumberHelpers;
import by.gto.library.helpers.SingleAppInstanceChecker;
import by.gto.xchanger.dao.XChangerDao;
import by.gto.xchanger.exceptions.DbObjectNotFoundException;
import by.gto.xchanger.exceptions.FileCorruptedException;
import by.gto.xchanger.exceptions.FileNotValidException;
import by.gto.xchanger.exceptions.LockException;
import by.gto.xchanger.exceptions.ProcessingErrorException;
import by.gto.xchanger.exceptions.RootXchangeException;
import by.gto.xchanger.exceptions.WrongMessageNumberException;
import by.gto.xchanger.exceptions.WrongVersionException;
import by.gto.xchanger.model.EntityDescriptor;
import by.gto.xchanger.model.FileInfo;
import by.gto.xchanger.model.XChangeCachedData;
import by.gto.xchanger.model.XChangeDBSpecificData;
import by.gto.xchanger.model.XChangeParameters;
import by.gto.xchanger.model.XChangeResults;
import by.gto.xchanger.model.XchangeOptions;
import by.gto.xchanger.parsers.SAXParserForVersion;
import by.gto.xchanger.storage.FileStorageSystem;
import by.gto.xchanger.storage.StorageSystem;
import by.gto.xml.entities.DiagCard;
import by.gto.xml.entities.MapsTransform;
import by.gto.xml.model.xchange.v1.Del;
import by.gto.xml.model.xchange.v1.Doc;
import by.gto.xml.model.xchange.v1.Msg;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Importer {
    static final Logger log = Logger.getLogger(Importer.class);
    public static final boolean DEBUG = System.getProperty("DEBUG") != null;

    @Autowired(required = true)
    private HashMap<StorageSystem, StorageSystem> storageSystems;

    //    @Autowired(required = true)
    //    private XChangerService service;

    @Autowired(required = true)
    protected TransactionTemplate txTemplate;

    @Autowired(required = true)
    protected XChangerDao dao;

    @Autowired(required = true)
    protected XChangeDBSpecificData dbSpecificData;

    protected String corruptedPath;
    protected String outdatedPath;
    protected String notvalidPath;
    protected String importErrorPath;
    protected String importedPath;
    protected String deletedPath;
    protected final static String filenamePattern = "(?i)^([\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12})_([\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12})_(\\d+)_(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})\\.xchange$";
    protected final static Pattern xmlXchangePattern = Pattern.compile("(?i)^([\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12})_([\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12})_(\\d+)_(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})\\.xml$", Pattern.CASE_INSENSITIVE);
    protected final static Pattern photoFileRegex = Pattern.compile("^([\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12})\\.(jpg|png|gif)$", Pattern.CASE_INSENSITIVE);
    private static LocalDateTime referencesLastUpdateTime = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0);
    protected XChangeCachedData cachedData;

    protected static JAXBContext jaxbContext = null;
    protected static JAXBContext jaxbBelTOv9 = null;
    protected static JAXBContext jaxbBelTOv10 = null;

    private static final String zeroGuidString = "00000000-0000-0000-0000-000000000000";

    static {
        try {
            jaxbContext = JAXBContext.newInstance(Msg.class);
            jaxbBelTOv9 = JAXBContext.newInstance(by.gto.xml.model.gto.v9.ResGTOXml.class);
            jaxbBelTOv10 = JAXBContext.newInstance(by.gto.xml.model.gto.v10.ResGTOXml.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private Importer() {
    }

    public void setDao(XChangerDao dao) {
        this.dao = dao;
    }

    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

    public void setDbSpecificData(XChangeDBSpecificData dbSpecificData) {
        this.dbSpecificData = dbSpecificData;
    }


    public static void main(String[] args) {
        System.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        System.setProperty("mail.smtp.socketFactory.port", "465");
        System.setProperty("mail.smtp.socketFactory.fallback", "false");
        System.setProperty("mail.smtp.port", "465");
        XchangeOptions options = analyzeArgs(args);
        String version = "FileImporter <version_unknown>";
        try (InputStream s = Importer.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
             InputStreamReader isr = new InputStreamReader(s);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Implementation-Version:")) {
                    version = "FileImporter " + line.split(":\\s+")[1];
                    break;
                }
            }
        } catch (Exception ignored) {
            log.error(ignored.getMessage(), ignored);
        }
        options.setAppVersion(version);
        log.info(version + " started");

        String springConfig = "spring/config.xml";

        if (DEBUG) {
            log.info("Debug config:" + springConfig);
        } else {
            log.info("Release config: " + springConfig);
        }

        try {
            ConfigurableApplicationContext appContext = new ClassPathXmlApplicationContext(springConfig);
            //listBeans();

            Importer imp = (Importer) appContext.getBean("importer");
            imp.imp(options);
        } catch (LockException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("End processing");
        }
    }

    //    private static void listBeans() {
    //        ApplicationContext ac = ApplicationContextProvider.getApplicationContext();
    //        for (String name : ac.getBeanDefinitionNames()) {
    //            Object o = ac.getBean(name);
    //            System.out.println(name + ": " + o.getClass().getName());
    //        }
    //    }

    public void imp(XchangeOptions options) {
        try {
            if (options.isOnlyLoadData() || options.isImportBelTO()) {
                StorageSystem ss = new FileStorageSystem();
                ss.setUrl(options.getOnlyLoadData_Dir());
                storageSystems.clear();
                storageSystems.put(ss, ss);
            }
            for (Map.Entry<StorageSystem, StorageSystem> pair : storageSystems.entrySet()) {
                if (options.isImportBelTO()) {
                    importBeltos(pair.getKey());
                } else {
                    makeXchange(pair.getKey(), pair.getValue(), options);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void importBeltos(StorageSystem ss) {
        XChangeParameters params = new XChangeParameters();
        ss.connect();
        prepare(null, params);
        params.getCachedData().setMinimumAllowedDate(LocalDate.of(2000, 1, 1));
        XChangeResults totalResult = new XChangeResults();
        List<String> fileNames = ss.dir("", "(?i).*\\.belto$");
        Date now = new Date();
        for (String filename : fileNames) {
            log.info("importing " + filename);
            try (InputStream is = ss.getInputStream(filename)) {
                Optional<XChangeResults> r1Optional = importBelto(is, params.getCachedData());
                if (r1Optional.isPresent()) {
                    XChangeResults r = r1Optional.get();
                    totalResult.getCertNumberSet().addAll(r.getCertNumberSet());
                    totalResult.getRegNumberSet().addAll(r.getRegNumberSet());
                }
                ss.renameTo(filename, String.format("complete/%1$tY-%1$tm-%1$td/%2$s", now, filename));
            } catch (MinimumAllowedDateViolationException ex) {
                log.error(ex.getMessage(), ex);
                ss.renameTo(filename, String.format("outdated/%1$tY-%1$tm-%1$td/%2$s", now, filename));
            } catch (EntityInvalidException ex) {
                log.error(ex.getMessage(), ex);
                ss.renameTo(filename, String.format("validateError/%1$tY-%1$tm-%1$td/%2$s", now, filename));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                ss.renameTo(filename, String.format("error/%1$tY-%1$tm-%1$td/%2$s", now, filename));
            }
        }
        // TODO: обрабатывать только что импортированные регномера и техпаспорта(вычищать из кэша Application server'а)
        //            totalResult.getRegNumberSet().forEach(rn -> {});
        //            totalResult.getCertNumberSet().forEach(cn -> {});
    }

    private Optional<XChangeResults> importBelto(InputStream is, XChangeCachedData cachedData) throws Exception {
        final byte[] zeroGuidBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] out = null;
        try (BufferedInputStream bis = new BufferedInputStream(is);
             ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis)) {
            ArchiveEntry ae;

            while ((ae = ais.getNextEntry()) != null) {
                if (isBelTOFile(ae.getName())) {
                    out = IOUtils.toByteArray(ais); // new String(out)
                    break;
                }
            }
        }
        if (out == null) {
            log.error("Archive doesn't contain xml entry");
            return Optional.empty();
        }
        if (!validateXchangeFile("Res_GTO", new int[]{10, 9}, out)) {
            throw new EntityInvalidException("validate error");
        }
        int fileVersion;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(out)) {
            fileVersion = SAXParserForVersion.parseForVersion(bis);
        }
        if (fileVersion < 9 || fileVersion > 10) {
            throw new EntityInvalidException("Поддерживаются версии 9 и 10");
        }

        final byte[] byteContent = out;
        final Exception[] exception = {null};
        Optional<XChangeResults> rOut = txTemplate.execute(status -> {
            XChangeResults result = new XChangeResults();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(byteContent)) {
                List<Doc> docs = null;
                Unmarshaller unmarshaller;
                switch (fileVersion) {
                    case 9:
                        unmarshaller = jaxbBelTOv9.createUnmarshaller();
                        by.gto.xml.model.gto.v9.ResGTOXml result9 = (by.gto.xml.model.gto.v9.ResGTOXml) unmarshaller.unmarshal(bais);
                        docs = result9.getDoc();
                        break;
                    case 10:
                        unmarshaller = jaxbBelTOv10.createUnmarshaller();
                        by.gto.xml.model.gto.v10.ResGTOXml result10 = (by.gto.xml.model.gto.v10.ResGTOXml) unmarshaller.unmarshal(bais);
                        docs = result10.getDoc();
                        break;
                }
                for (Doc doc : docs) {
                    long dsCode = doc.getDsCode();
                    if (dsCode == 999999999 || dsCode == 999999998) {
                        log.info("не принимаю данные от станций с id=999999998, 999999999");
                        continue;
                    }

                    byte[] guid = GuidHelpers.guidAsBytes(doc.getGuid());
                    if (Arrays.equals(zeroGuidBytes, guid)) {
                        log.error("Importing of PTI with guid 00000000-0000-0000-0000-000000000000 is not allowed");
                        continue;
                    }
                    int r = insertOrUpdatePti(doc, false, cachedData, false);

                    if (r == 0) {
                        log.error("Error inserting or updating record guid = " + doc.getGuid());
                        throw new ProcessingErrorException("Update or insert didn't affect any rows");
                    }
                    result.getCertNumberSet().add(BlankNumberHelpers.format(doc.getTcSeria(),
                        NumberUtils.toInt(doc.getTcNumber(), 0), 0, true));
                    RegNumberParseResult regNumberParseResult = RegNumberHelpers.parseRegNumber(doc.getRegNumber());
                    if (regNumberParseResult.isSuccess()) {
                        result.getRegNumberSet().add(RegNumberHelpers.format(regNumberParseResult, true));
                    }
                }

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                status.setRollbackOnly();
                exception[0] = ex;
            }
            return Optional.of(result);
        });
        if (exception[0] != null) {
            throw exception[0];
        }
        return rOut;
    }

    private void getLock() throws LockException {
        if (!SingleAppInstanceChecker.getLock(Importer.class)) {
            throw new LockException("Lock is not acquired. Another instance running?");
        }
    }

    public void prepare(File f, XChangeParameters params) {
        log.info("Updating references from DB");
        this.cachedData = new XChangeCachedData();
        this.cachedData.setMapFirmCodeToId(dao.getCodesToIdsMaping());
        this.cachedData.setChecksMap(dao.getChecksMapping());
        this.cachedData.setMapOurModelIdToGaiModelId(dao.getMapOurModelIdToGaiModelId());
        Optional<Date> optAllowed = dao.getMinimumAllowedDate();
        Date allowed = optAllowed.orElseGet(() -> DateHelpers.calculateDefaultMinimumAllowedDate(2));
        this.cachedData.setMinimumAllowedDate(allowed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        params.setCachedData(this.cachedData);
        String dbVersion = dao.getDBVersion();
        params.setDbVersion(dbVersion);
        params.setAppVersion(dbVersion);
    }

    private XChangeResults makeXchange(StorageSystem inSS, StorageSystem outSS, XchangeOptions options) throws RootXchangeException {
        getLock();
        try {
            XChangeParameters params = new XChangeParameters();
            if (referencesLastUpdateTime.plusHours(5).isBefore(LocalDateTime.now())) {
                prepare(null, params);
                referencesLastUpdateTime = LocalDateTime.now();
            }
            byte[] myGUID = dao.getMyGuid();

            params.setInputStorageSystem(inSS);
            params.setOutputStorageSystem(outSS);
            params.setAppVersion(options.getAppVersion());
            params.setDbVersion(dao.getDBVersion());
            params.setCachedData(cachedData);
            params.setMyID(myGUID);

            inSS.connect();
            outSS.connect();

            initializeDirs(inSS);

            Set<UUID> setOfSenders = new HashSet<>();

            List<String> files = new ArrayList<>(inSS.dir("", filenamePattern));
            files.sort(String::compareTo);

            if (!options.isOnlyLoadData()) {
                HashMap<String, FileInfo> versions = new HashMap<>();
                for (String file : files) {
                    String g = file.replaceAll(filenamePattern, "$2");
                    Integer v = Integer.valueOf(file.replaceAll(filenamePattern, "$3"));
                    if (versions.containsKey(g)) {
                        if (versions.get(g).version < v) {
                            inSS.renameTo(versions.get(g).name, deletedPath + "/" + versions.get(g).name);
                            log.info(versions.get(g).name + " deleted");
                            versions.put(g, new FileInfo(v, file));
                        } else {
                            inSS.renameTo(file, deletedPath + "/" + file);
                            log.info(file + " deleted");
                        }
                    } else {
                        versions.put(g, new FileInfo(v, file));
                    }
                }

                files.clear();
                for (Map.Entry<String, FileInfo> entry : versions.entrySet()) {
                    files.add(entry.getValue().name);
                }
            }

            XChangeResults result = new XChangeResults();
            for (String file : files) {
                XChangeResults results1file = import1file(file, params, options);

                if (!results1file.isReceiptOnly()) {
                    result.getRegNumberSet().addAll(results1file.getRegNumberSet());
                    result.getCertNumberSet().addAll(results1file.getCertNumberSet());
                    setOfSenders.add(GuidHelpers.guidFromBytes(results1file.getImportedSenderGUID()));
                }
            }

            if (options.isOnlyLoadData()) {
                log.info("Import complete. Omitting export (because flag --only-load-data specified)");
            } else {
                log.info("Import complete. starting export");

                makeExport(setOfSenders, params);
                log.info("Export complete");
            }
            return result;
        } finally {
            try {
                dbSpecificData.finish();
            } catch (Exception ignored) {
            }
            releaseLock();
        }
    }

    public Integer releaseLock() {
        return null;
    }

    public void makeExport(Set<UUID> senders, XChangeParameters params) {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder;
        params.setExportDate(new Date());
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
            return;
        }
        int peersMadeInactive = dao.inactivateOldPeers(14);
        log.info("" + peersMadeInactive + " peer(s) made inactive");
        List<byte[]> additionalPeers = dao.findAdditionalPeers(params.getExportDate());
        senders.addAll(additionalPeers.stream().map(GuidHelpers::guidFromBytes).collect(Collectors.toList()));
        Iterator<UUID> sendersIterator = senders.iterator();
        while (sendersIterator.hasNext()) {
            final UUID senderId = sendersIterator.next();
            txTemplate.execute(status -> {
                try {
                    exportForSinglePeer(GuidHelpers.guidAsBytes(senderId), docBuilder, params);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    status.setRollbackOnly();
                }
                return null;
            });
        }
    }

    private void makeHeader(Document doc, Element rootElement, int msgNumber, int msgNumberReceived,
                            XChangeParameters params, byte[] senderId, int msgFormatVersion) {
        Element hdr = doc.createElement("hdr");
        rootElement.appendChild(hdr);

        Attr at = doc.createAttribute("ver");
        at.setValue(String.valueOf(msgFormatVersion));
        hdr.setAttributeNode(at);

        at = doc.createAttribute("from");
        at.setValue(GuidHelpers.stringGuidFromBytes(params.getMyID()));
        hdr.setAttributeNode(at);

        at = doc.createAttribute("to");
        at.setValue(GuidHelpers.stringGuidFromBytes(senderId));
        hdr.setAttributeNode(at);

        at = doc.createAttribute("date");
        at.setValue(String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", new Date()));
        hdr.setAttributeNode(at);

        at = doc.createAttribute("msg_number");
        at.setValue(String.valueOf(msgNumber));
        hdr.setAttributeNode(at);

        at = doc.createAttribute("received");
        at.setValue(String.valueOf(msgNumberReceived));
        hdr.setAttributeNode(at);
    }

    private void makeInfo(Document doc, Element rootElement, XChangeParameters params) {
        Element info = doc.createElement("Info");
        rootElement.appendChild(info);

        Attr at = doc.createAttribute("Version_App");
        at.setValue(params.getAppVersion());
        info.setAttributeNode(at);

        at = doc.createAttribute("Version_App_DB");
        at.setValue(params.getDbVersion());
        info.setAttributeNode(at);

        at = doc.createAttribute("Version_DB");
        at.setValue(params.getDbVersion());
        info.setAttributeNode(at);
    }

    private void exportEntity(Document doc, byte[] peerId, EntityDescriptor entityDescriptor, Date exportDate) {
        List<Integer> deletionsList = dao.getDeletions(entityDescriptor.getEntityName(), peerId, exportDate);
        List<Map<String, Object>> changesList = dao.getChanges(entityDescriptor, peerId, exportDate);
        String entityName = entityDescriptor.getEntityName();
        if (deletionsList.size() != 0 || changesList.size() != 0) {
            Element deletions = doc.createElement("deletions");
            Element changes = doc.createElement("changes");
            Element r = doc.createElement(entityName);
            for (Integer delId : deletionsList) {
                Element child = doc.createElement("del");
                Attr attr = doc.createAttribute("name");
                attr.setValue(entityName);
                child.setAttributeNode(attr);

                attr = doc.createAttribute("guid");
                attr.setValue(String.valueOf(delId));
                child.setAttributeNode(attr);

                deletions.appendChild(child);
            }
            for (Map<String, Object> entry : changesList) {
                Element child = doc.createElement(entityName);
                appendAttributeTo(child, "guid", entry.get(entityDescriptor.getIdFieldName()));
                appendAttributeTo(child, "valid", entry.get("valid"));
                for (Map.Entry<String, String> pair : entityDescriptor.getFieldsToExport().entrySet()) {
                    appendAttributeTo(child, pair.getValue(), entry.get(pair.getValue()));
                }
                //                for (Map.Entry<String, Object> field : entry.entrySet()) {
                //                    Attr attr = doc.createAttribute(field.getKey());
                //                    Object val = field.getValue();
                //                    attr.setValue((val == null) ? "" : val.toString());
                //                    child.setAttributeNode(attr);
                //                }
                changes.appendChild(child);
            }
            r.appendChild(deletions);
            r.appendChild(changes);
            doc.getFirstChild().appendChild(r);
        }
    }

    private void appendAttributeTo(Element child, String name, Object val) {
        Document doc = child.getOwnerDocument();
        Attr attr = doc.createAttribute(name);
        if (val == null) {
        } else if (val instanceof Boolean) {
            if ((Boolean) val) {
                attr.setValue("1");
            } else {
                attr.setValue("0");
            }
        } else {
            attr.setValue(val.toString());
        }
        child.setAttributeNode(attr);
    }

    public void exportForSinglePeer(byte[] senderId, DocumentBuilder docBuilder, XChangeParameters params) throws Exception {
        UUID sGuid = GuidHelpers.guidFromBytes(senderId);
        log.info("Export started for " + sGuid);
        List<Integer> r1 = dao.getMessageNumbers(senderId);

        if (r1.size() == 0) {
            throw new DbObjectNotFoundException("Can't find peer " + sGuid);
        }
        int msgNumber = r1.get(0) + 1;
        int msgNumberReceived = r1.get(1);
        int msgFormatVersion = r1.get(2);

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("msg");
        doc.appendChild(rootElement);

        makeHeader(doc, rootElement, msgNumber, msgNumberReceived, params, senderId, msgFormatVersion);
        if (msgFormatVersion > 1) {
            makeInfo(doc, rootElement, params);
        }
        dao.updateLastSentMessage(senderId, msgNumber);
        log.info("Enumerating changes");

        // тут надо пройтись по changes_registry и выгрузить для адресата какие-нибудь данные, если есть...
        params.setExportDate(new Date());
        dbSpecificData.getEntities().stream().forEach(e -> exportEntity(doc, senderId, e, params.getExportDate()));

        log.info("Enumerating changes finished. Updating changes_registry");
        dao.updateChangesRegistry(senderId, msgNumber, params.getExportDate());
        log.info("Updating changes_registry finished");
        String answerFilename =
            String.format("%1$s_%2$s_%3$d_%4$tY-%4$tm-%4$td-%4$tH-%4$tM-%4$tS",
                sGuid, GuidHelpers.guidFromBytes(params.getMyID()),
                msgNumber, new Date());
        log.info("Writing exchange file " + answerFilename);
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        try (OutputStream os1 = params.getOutputStorageSystem().getOutputStream(answerFilename + ".xchange");
             ArchiveOutputStream aos = new ZipArchiveOutputStream(os1)) {
            ArchiveEntry ae = new ZipArchiveEntry(answerFilename + ".xml");
            aos.putArchiveEntry(ae);
            StreamResult result = new StreamResult(aos);
            transformer.transform(source, result);
            aos.closeArchiveEntry();
        }
    }

    private boolean isXchangeFile(String name) {
        return xmlXchangePattern.matcher(name).matches();
    }

    private boolean isBelTOFile(String name) {
        return "res_gto.xml".equals(StringUtils.lowerCase(name));
    }

    private boolean isPhotoFile(String fileName) {
        fileName = fileName.toLowerCase();
        return photoFileRegex.matcher(fileName).matches();
    }

    public static String[] constructPhotoName(String name) throws IndexOutOfBoundsException {
        if (null == name) {
            return null;
        }
        char[] chars = name.toCharArray();
        System.arraycopy(chars, 14, chars, 13, 22);
        System.arraycopy(chars, 23, chars, 22, 4);
        chars[8] = '/';
        chars[17] = '/';
        chars[26] = '/';
        return new String[]{
            new String(chars, 0, 35),
            new String(chars, 0, 26)
        };
    }

    public XChangeResults importMsg(final Msg msg, XChangeParameters params, XchangeOptions options) throws MinimumAllowedDateViolationException {
        final byte[] zeroGuidBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] msgTo;
        msgTo = GuidHelpers.guidAsBytes(msg.getHdr().getTo());
        if (Arrays.equals(msgTo, zeroGuidBytes)) {
            log.error("msgTo is " + zeroGuidString);
            return null;
        }
        if (!Arrays.equals(msgTo, params.getMyID())) { // сообщение на самом деле не нам!
            throw new FileNotValidException("The message is not addressed to us");
        }
        int msgFormatVersion = (int) msg.getHdr().getVer();
        if (msgFormatVersion > 3) {
            throw new WrongVersionException("Can only parse message of versions 1..3");
        }
        byte[] msgFrom = GuidHelpers.guidAsBytes(UUID.fromString(msg.getHdr().getFrom()));
        if (Arrays.equals(zeroGuidBytes, msgFrom)) { // Ошибка в файле выгрузки
            log.error("msgFrom is " + zeroGuidString);
            return null;
        }
        XChangeResults result = new XChangeResults();
        result.setImportedSenderGUID(msgFrom);
        boolean receiptOnly = true;
        long msgNumber = msg.getHdr().getMsgNumber();
        long msgSuccReceived = msg.getHdr().getReceived();
        if (!options.isOnlyLoadData()) {
            Integer receivedMessageNumber = dao.getReceivedMessageNumber(msgFrom);
            if (receivedMessageNumber == null) {
                log.info("Creating peer");
                // такого партнера по репликации еще не было - создаем:
                dao.createPeer(msgFrom, msgFormatVersion);
                registerReferencesForPeer(msgFrom);
                log.info("Create peer done");
            } else if (receivedMessageNumber >= msgNumber) {
                throw new WrongMessageNumberException(
                    String.format("message number %d <= last received message (%d) from peer %s",
                        msgNumber, receivedMessageNumber, GuidHelpers.guidFromBytes(msgFrom)));
            }
        }

        // удаление:
        if (msg.getPTI() != null && msg.getPTI().getDeletions() != null) {
            log.info("" + msg.getPTI().getDeletions().getDel().size() + " записей для удаления");
            for (Del del : msg.getPTI().getDeletions().getDel()) {
                byte[] guidToDelete = GuidHelpers.guidAsBytes(del.getGuid());
                if (Arrays.equals(guidToDelete, zeroGuidBytes)) {
                    log.error("Deleting of PTI with guid " + zeroGuidString + " is not allowed");
                    receiptOnly = false;
                    continue;
                }

                log.info("Deleting PTI with guid " + del.getGuid());
                log.info("    " + dao.deletePti(guidToDelete, params.getCachedData().getMinimumAllowedDate()) + " records affected");
                receiptOnly = false;
            }
        }

        Long dsCode = null;
        // вставка/обновление:
        if (msg.getPTI() != null && msg.getPTI().getChanges() != null) {
            log.info("" + msg.getPTI().getChanges().getDoc().size() + " записей для импорта");
            for (Doc doc : msg.getPTI().getChanges().getDoc()) {
                dsCode = doc.getDsCode();
                if (dsCode == 999999999 || dsCode == 999999998) {
                    receiptOnly = false;
                    continue;
                }

                byte[] guid = GuidHelpers.guidAsBytes(doc.getGuid());
                if (Arrays.equals(zeroGuidBytes, guid)) {
                    log.error("Importing of PTI with guid 00000000-0000-0000-0000-000000000000 is not allowed");
                    receiptOnly = false;
                    continue;
                }
                int r = insertOrUpdatePti(doc, msg.getHdr().getVer() == 3, params.getCachedData(), !options.isOnlyLoadData());

                if (r == 0) {
                    log.error("Error inserting or updating record guid = " + doc.getGuid());
                    throw new ProcessingErrorException("Update or insert didn't affect any rows");
                }
                result.getCertNumberSet().add(BlankNumberHelpers.format(doc.getTcSeria(),
                    NumberUtils.toInt(doc.getTcNumber(), 0), 0, true));
                RegNumberParseResult regNumberParseResult = RegNumberHelpers.parseRegNumber(doc.getRegNumber());
                if (regNumberParseResult.isSuccess()) {
                    result.getRegNumberSet().add(RegNumberHelpers.format(regNumberParseResult, true));
                }
                receiptOnly = false;
            }
        }
        if (!options.isOnlyLoadData()) {
            dao.updateRegistry(msgFrom, (int) msgSuccReceived);
            if (Objects.equals(false, dao.isPeerActive(msgFrom))) {
                registerReferencesForPeer(msgFrom);
            }
            dao.updateMessageNumberInPeerTable(msgFrom, (int) msgNumber, dsCode, msgFormatVersion);
        }

        // если это сообщение - всего лишь квитанция, т.е. не содержит информации о сущностях
        // то возвращаем null. В этом случае вызывающая функция не будет формировать ответ отправителю,
        // чтобы не меняться бесконечно пустыми квитанциями:
        result.setReceiptOnly(receiptOnly);
        return result;
    }

    /**
     * Зарегистрировать для указанного пира все записи всех отслеживаемых справочников.
     * Нужно для свежесозданных пиров и тех, у кого статус изменился с неактивного на активный.
     * @param peerGuid идентификатор пира.
     */
    private void registerReferencesForPeer(byte[] peerGuid) {
        for (EntityDescriptor entityDescriptor : dbSpecificData.getEntities()) {
            dao.registerReferenceForPeer(peerGuid, entityDescriptor);
        }
    }

    private int insertOrUpdatePti(
        Doc doc, boolean useGaiModelId, XChangeCachedData cachedData,
        boolean loadedByProtocol) throws MinimumAllowedDateViolationException {
        DiagCard dc = MapsTransform.transformV9(doc, cachedData.getMapFirmCodeToId(), cachedData.getChecksMap());
        if (dc.getDATETO().toLocalDate().compareTo(cachedData.getMinimumAllowedDate()) < 0) {
            throw new MinimumAllowedDateViolationException("Минимальная дата изменений " + cachedData.getMinimumAllowedDate());
        }
        if (useGaiModelId) {
            // Особый случай: код для "прочих" преобразуем: 1 => -1005
            final int gi = doc.getModelId().intValue();
            dc.setGaiModelId(gi == 1 ? -1005 : gi);
        } else {
            Integer gaiModelId = cachedData.getMapOurModelIdToGaiModelId().get(dc.getIDMODELSTC());
            dc.setGaiModelId((gaiModelId == null) ? 0 : gaiModelId);
        }

        ChecksV3 checksObject = new ChecksV3();
        checksObject.parse(dc.getChecks(), dc.getNotes(), dc.getMismatches());
        dc.setChecksPacked(checksObject.returnPacked());
        return dao.insertOrUpdatePTI(dc, loadedByProtocol);
    }


    public XChangeResults import1file(String filename, XChangeParameters params, XchangeOptions options) {
        log.info("importing " + filename);
        boolean storePhotos = false;
        if (options.getPhotosDirectory() != null) {
            File f = new File(options.getPhotosDirectory());
            if (f.exists() && f.isDirectory()) {
                storePhotos = true;
            }
        }
        StorageSystem inStorageSystem = params.getInputStorageSystem();
        try {
            byte[] out = null;
            try (InputStream is = params.getInputStorageSystem().getInputStream(filename);
                 BufferedInputStream bis = new BufferedInputStream(is);
                 ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis)) {
                ArchiveEntry ae;

                while ((ae = ais.getNextEntry()) != null) {
                    if (storePhotos && isPhotoFile(ae.getName())) {
                        String[] strings = constructPhotoName(ae.getName());
                        String imageName = options.getPhotosDirectory() + "/" + strings[0];
                        FileOutputStream fos = null;
                        try {
                            Files.createDirectories(Paths.get(options.getPhotosDirectory(), strings[1]));
                            fos = new FileOutputStream(imageName);
                            IOUtils.copy(ais, fos);
                        } catch (Exception e) {
                            log.error("failed to store " + imageName, e);
                        } finally {
                            if (null != fos) {
                                fos.close();
                            }
                        }
                    }
                    if (isXchangeFile(ae.getName())) {
                        out = IOUtils.toByteArray(ais);
                    }
                }

                if (out == null) {
                    throw new ArchiveException("Archive doesn't contain xml entry");
                }
            }

            if (!validateXchangeFile("xchange", new int[]{3, 2, 1}, out)) {
                throw new FileNotValidException("validate error");
            }

            Msg msg;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(out)) {
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                msg = (Msg) jaxbUnmarshaller.unmarshal(bais);
            }
            XChangeResults result = txTemplate.execute(status -> {
                try {
                    return importMsg(msg, params, options);
                } catch (MinimumAllowedDateViolationException ex) {
                    status.setRollbackOnly();
                    XChangeResults xChangeResults = new XChangeResults();
                    xChangeResults.setError(ex);
                    return xChangeResults;
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            });
            if (result != null && result.getError().isPresent()) {
                throw result.getError().get();
            }

            inStorageSystem.renameTo(filename, importedPath + filename);
            return result;
        } catch (Throwable ex) {
            log.error(filename + " " + ex.getMessage(), ex);
            try {
                boolean result;
                if (ex instanceof FileNotValidException
                    || ex instanceof JAXBException) {
                    result = inStorageSystem.renameTo(filename, notvalidPath + filename);
                    log.info("moving to " + notvalidPath + filename + " " + (result ? "success" : "failed"));
                } else if (ex instanceof MinimumAllowedDateViolationException) {
                    result = inStorageSystem.renameTo(filename, outdatedPath + filename);
                    log.info("moving to " + outdatedPath + filename + " " + (result ? "success" : "failed"));
                } else if (ex instanceof FileCorruptedException
                    || ex instanceof ArchiveException
                    || ex instanceof IOException) {//  || ex is InvalidOperationException
                    result = inStorageSystem.renameTo(filename, corruptedPath + filename);
                    log.info("moving to " + corruptedPath + filename + " " + (result ? "success" : "failed"));
                } else {
                    result = inStorageSystem.renameTo(filename, importErrorPath + filename);
                    log.info("moving to " + importErrorPath + filename + " " + (result ? "success" : "failed"));
                }
            } catch (Exception ex2) {
                log.error("Error during file operation:\n" + ex2.getMessage(), ex2);
            }
        }
        return null;
    }

    private boolean validateXchangeFile(String baseName, int[] versions, byte[] content) throws IOException {
        boolean fileValid = false;
        for (int ver : versions) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                if (validateXSD(baseName, ver, bais)) {
                    fileValid = true;
                    break;
                }
            }
        }
        return fileValid;
    }

    public boolean validateXSD(String baseName, int version, InputStream xmlStream) {
        try {
            URL url = getClass().getClassLoader().getResource(String.format("files%s%s_v%d.xsd", "/", baseName, version));
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(url);

            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlStream));
            return true;
        } catch (Exception ex) {
            log.info(ex.getMessage(), ex);
            return false;
        }
    }

    private static XchangeOptions analyzeArgs(String[] args) {
        XchangeOptions result = new XchangeOptions();
        for (String arg : args) {
            String lowercaseArg = arg.toLowerCase();
            if (lowercaseArg.startsWith("--only-load-data=")) {
                result.setOnlyLoadData(true);
                result.setOnlyLoadData_Dir(arg.split("=")[1]);
            } else if (lowercaseArg.startsWith("--import-belto=")) {
                result.setImportBelTO(true);
                result.setOnlyLoadData_Dir(arg.split("=")[1]);
            } else if (lowercaseArg.equals("--help") || lowercaseArg.equals("-h")) {
                System.out.println("\n\nusage:");
                System.out.println("java -jar FileImporter-x.y.z.jar [options]");
                System.out.println("Possible options:");
                System.out.println("    --version, -v: Print version information and exit");
                System.out.println("    --help, -h: print this help and exit");
                System.out.println("    --only-load-data=path: only load ALL files from [path] as is, without\n"
                    + "   taking into account  message versions and creting anwser. DB field LOADED_BY_PROTOCOL = 0");
                System.out.println("    --import-belto=path: load *.belto files from [path]. DB field LOADED_BY_PROTOCOL = 0");
                System.exit(0);
            } else if (lowercaseArg.equals("--version") || lowercaseArg.equals("-v")) {
                System.out.println("\n\n" + Version.ARTIFACTID + " v" + Version.BUILD + " " + Version.DATEBUILD);
                System.exit(0);
            } else {
                System.out.println("\n\nUnknown option: " + arg);
                System.exit(1);
            }
        }
        return result;
    }

    public void setStorageSystems(HashMap<StorageSystem, StorageSystem> storageSystems) {
        this.storageSystems = storageSystems;
    }

    protected void initializeDirs(StorageSystem inStorageSystem) {
        String subfolder = String.format("%%s%2$c%1$TY-%1$Tmm%2$c%1$Tdd%2$c%1$THh%2$c", new Date(), File.separatorChar);

        corruptedPath = String.format(subfolder, "corrupted");
        //inStorageSystem.connect();
        //inStorageSystem.mkdirs(corruptedPath);

        notvalidPath = String.format(subfolder, "notvalid");
        //inStorageSystem.mkdirs(notvalidPath);

        importErrorPath = String.format(subfolder, "importError");
        //inStorageSystem.mkdirs(importErrorPath);

        importedPath = String.format(subfolder, "imported");
        //inStorageSystem.mkdirs(importedPath);

        deletedPath = String.format(subfolder, "deleted");
        outdatedPath = String.format(subfolder, "outdated");

        //inStorageSystem.disconnect();
    }

}
