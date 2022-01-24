package parsers;

import by.gto.erip.exceptions.SAXTerminatorException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParserForVersion extends DefaultHandler {

    private boolean versionTag = false;
    private int version = -1;
    private final StringBuilder strVersion = new StringBuilder();

    public int getVersion() {
        return version;
    }

    public static int parseForVersion(InputStream bis) {
        SAXParserForVersion contentHandler = null;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        try {
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            contentHandler = new SAXParserForVersion();
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(new InputSource(bis));
        } catch (SAXTerminatorException ex) {
            return contentHandler.getVersion();
        } catch (ParserConfigurationException | SAXException | IOException ignored) {
        }
        return -1;
    }

    @Override
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
        throws SAXException {
        if (localName.equals("Version")) {
            versionTag = true;
        }
    }

    @Override
    public void endElement(String uri,
                           String localName,
                           String qName)
        throws SAXException {
        if (localName.equals("Version")) {
            try {
                version = Integer.parseInt(strVersion.toString().trim());
            } catch (Exception e) {
                version = -1;
            }
            throw new SAXTerminatorException();
        }
    }

    @Override
    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException {
        if (versionTag) {
            strVersion.append(ch, start, length);
        }
    }
}

