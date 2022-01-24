package model.xml.v11;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import model.xml.DateGenerated;
import model.xml.IExchangeFile;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "dateGenerated",
        "info",
        "version",
        "doc"
})
@XmlRootElement(name = "Res_GTO.xml")
public class ResGTOXml implements IExchangeFile {

    @XmlElement(name = "DateGenerated")
    protected DateGenerated dateGenerated;
    @XmlElement(name = "Info")
    protected Object info;
    @XmlElement(name = "Version")
    @XmlSchemaType(name = "unsignedByte")
    protected Short version;
    @XmlElement(name = "Doc")
    protected List<Doc> doc;

    @Override
    public String getDateGenerated() {
        return dateGenerated.toString();
    }

    /**
     * Gets the value of the info property.
     *
     * @return possible object is
     * {@link Object }
     */
    public Object getInfo() {
        return info;
    }

    @Override
    public int getVersion() {
        return version.intValue();
    }

    /**
     * Gets the value of the doc property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the doc property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDoc().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link model.xml.v11.Doc}
     *
     * @return bla
     */
    public List<Doc> getDoc() {
        if (doc == null) {
            doc = new ArrayList<>();
        }
        return this.doc;
    }

    @Override
    public String getDateRange() {
        return dateGenerated.getDateRange();
    }

    @Override
    public List getDocs() {
        return getDoc();
    }

}
