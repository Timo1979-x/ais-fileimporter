package main;

import by.gto.erip.exceptions.EntityInvalidException;
import by.gto.erip.model.ChecksV3;
import by.gto.library.helpers.BlankNumberHelpers;
import by.gto.library.helpers.GuidHelpers;
import by.gto.library.helpers.TranslitFast;
import dao.Dao;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import model.db.DatabaseTiEntity;
import model.xml.v11.ResGTOXml;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import parsers.SAXParserForVersion;
import model.xml.v11.Doc;

public class Main {
    static final Logger log = Logger.getLogger(Main.class);
    final static byte[] zeroGuidBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    protected static final DateTimeFormatter SDF_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy kk:mm:ss");
    private static Dao dao;

    public static void main(String[] args) throws Exception {
        File fDir = new File(args[0]);
        final String[] fileNames = fDir.list((dir1, name) -> name.matches("(?i).*\\.belto$"));
        if (fileNames == null)
            return;
        prepare();
        for (String fileName : fileNames) {
            byte[] byteContent = null;
            final String fullPath = args[0] + "/" + fileName;
            try (
                    FileInputStream fis = new FileInputStream(fullPath);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis)) {
                ArchiveEntry ae;

                while ((ae = ais.getNextEntry()) != null) {
                    if ("res_gto.xml".equals(StringUtils.lowerCase(ae.getName()))) {
                        byteContent = IOUtils.toByteArray(ais);
                        if (!validate(byteContent)) {
                            byteContent = null;
                        }
                        break;
                    }
                }
            }
            try {
                if (byteContent == null) {
                    log.error("Archive doesn't contain xml entry or xml is invalid");
                    Files.move(Paths.get(fullPath), Paths.get(fullPath + ".invalid"));
                    continue;
                }
                int fileVersion;
                try (ByteArrayInputStream bis = new ByteArrayInputStream(byteContent)) {
                    fileVersion = SAXParserForVersion.parseForVersion(bis);
                }
                if (fileVersion < 11 || fileVersion > 12) {
                    throw new EntityInvalidException("Поддерживается только версии 11, 12");
                }
                imp(byteContent);

                Files.move(Paths.get(fullPath), Paths.get(fullPath + ".imported"));
            } catch (Exception ex) {
                log.error("Ошибка импорта", ex);
            }
        }
    }

    private static void imp(byte[] byteContent) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteContent)) {
            JAXBContext jaxbBelTOv11 = JAXBContext.newInstance(ResGTOXml.class);
            final Unmarshaller unmarshaller = jaxbBelTOv11.createUnmarshaller();
            ResGTOXml result = (ResGTOXml) unmarshaller.unmarshal(bais);
            for (Doc doc : (List<Doc>) result.getDocs()) {
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
                DatabaseTiEntity dbEntity = convert(doc);
                dao.insertTi(dbEntity);
            }
            dao.connection.commit();
        }
    }

    private static DatabaseTiEntity convert(Doc docV11) throws SQLException {
        DatabaseTiEntity converted = new DatabaseTiEntity();
        converted.ds_id = docV11.getDsCode();
        converted.vehicle_type_id = docV11.getIdTsType();
        converted.vehicle_engine_type_id = docV11.getIdTsTypeEngine();
        converted.color_id = docV11.getIdTsColour();
        converted.card_series = docV11.getDcSeria();
        if (converted.card_series != null) {
            converted.card_series = TranslitFast.translitRus2LatWithUppercase(converted.card_series);
        }
        converted.card_number = docV11.getDcNumber();
        converted.check_number = (byte) (docV11.getNumInspection() + 1);

        String regCertSeries = StringUtils.stripToNull(docV11.getTcSeria());
        Integer regCertNumber = docV11.getTcNumber();
        if (regCertSeries == null || regCertNumber == null) {
            converted.reg_cert_series = "";
            converted.reg_cert_number = 0;
        } else {
            converted.reg_cert_series = BlankNumberHelpers.normalizeBlankSeries(regCertSeries);
            converted.reg_cert_number = regCertNumber;
        }

        switch (docV11.getConclusion()) {
            // не соотв.
            case 2:
                converted.conclusion = 0;
                break;
            // соотв.
            case 1:
                converted.conclusion = 1;
                break;
            // соотв. с замеч.
            case 3:
                converted.conclusion = 2;
                break;

        }
        converted.conclusion = (byte) (docV11.getConclusion() - 1);
        LocalDateTime dateTO;
        try {
            dateTO = LocalDateTime.parse(docV11.getDateto(), SDF_DATE);
        } catch (DateTimeParseException ex) {
            dateTO = LocalDateTime.now();
        }
        converted.ti_date = dateTO;
        converted.version = 3;
        converted.guid = GuidHelpers.guidAsBytes(docV11.getGuid());
        converted.kilometrage = docV11.getMileage();
        converted.weight = docV11.getWeight();
        converted.measurement_method = docV11.getMethodIzm();
        converted.ecological_class = docV11.getEkoClass() != null ? docV11.getEkoClass() : 0;
        converted.loaded_by_protocol = true;
        converted.category_id = docV11.getIdTsCateg();
        converted.reg_number = docV11.getRegNumber();

        String vin, chassis;
        if (docV11.getVinIsChassi() != 0) {
            vin = "";
            chassis = docV11.getVin();
        } else {
            chassis = "";
            vin = docV11.getVin();
        }
        converted.vin = vin;
        converted.year = docV11.getDateauto();
        converted.applying = docV11.getIdTsUse();
        converted.center_generated_guid = false;
        converted.total = docV11.getSymmaSNds();
        converted.vat = docV11.getSymmaNds();
        converted.flags = 0;
        converted.source = null;
        converted.dl = docV11.getDl();
        converted.act_number = null;

        converted.generated.employee_id = dao.findOrInsertEmployee(docV11.getUserName());
        converted.generated.vehicle_id = dao.findOrInsertVehicle(converted.year, vin, chassis);

        converted.generated.owner_id = dao.findOrInsertClient(NumberUtils.toInt(docV11.getOwnerType(), 1) - 1, docV11.getOwnerName(), docV11.getOwnerUnp());
        converted.generated.holder_id = dao.findOrInsertClient(0, docV11.getParent(), "");
        converted.generated.customer_id = dao.findOrInsertClient(NumberUtils.toInt(docV11.getCustomerType(), 1) - 1, docV11.getCustomerName(), docV11.getCustomerUnp());
        converted.generated.reg_number1_id = dao.findOrInsertRegnumber(docV11.getRegNumber());
        converted.generated.reg_number2_id = null;
        converted.generated.dl_digits = BlankNumberHelpers.extractLongestDigitsSequence(docV11.getDl());
        converted.generated.payment_set_id = null;
        converted.generated.possibly_wrong_conclusion = 0;
        converted.generated.model_id = docV11.getModelId();
        converted.generated.checks = convertChecksV3(docV11.getCheck(), docV11.getNotes(), docV11.getMismatch());
        return converted;
    }

    private static byte[] convertChecksV3(String allChecks, String notes, String mismatch) {
        ChecksV3 checks = new ChecksV3();
        checks.parse(allChecks, notes, mismatch);
        return checks.returnPacked();
    }

    private static boolean validate(byte[] content) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
            URL url = Main.class.getClassLoader().getResource("files/Res_GTO_v11.xsd");
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(url);

            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(bais));
            return true;
        } catch (Exception ex) {
            log.info(ex.getMessage(), ex);
        }
        return false;
    }

    public static void prepare() throws Exception {
        final String user = System.getenv("IMPORT_USERNAME");
        final String password = System.getenv("IMPORT_PASSWORD");
        String jdbcUrl = System.getenv("IMPORT_JDBC_URI");
        jdbcUrl = jdbcUrl != null ? jdbcUrl : "jdbc:mariadb://127.0.0.1:3306/ti";
        if (user == null || password == null) {
            System.out.println("Usage:\n  java -jar import-belto-v11.jar <directory with exchange files>");
            System.out.println("Before launch you MUST set environment variables IMPORT_USERNAME, IMPORT_PASSWORD and optionally IMPORT_JDBC_URI.");
            System.out.println("Default for IMPORT_JDBC_URI is jdbc:mariadb://127.0.0.1:3306/ti");
            System.exit(-1);
        }

        Class.forName("org.mariadb.jdbc.Driver");
        final Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
        conn.setAutoCommit(false);
        dao = new Dao(conn);
    }
}
