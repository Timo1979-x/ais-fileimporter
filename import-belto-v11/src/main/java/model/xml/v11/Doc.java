package model.xml.v11;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import model.xml.BigDecimalXmlAdapter;
import model.xml.IDiagCard;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Doc")
public class Doc implements IDiagCard {

    @XmlAttribute(name = "id_dso_diagn_card")
    @XmlSchemaType(name = "unsignedInt")
    protected Long idDsoDiagnCard;
    @XmlAttribute(name = "guid", required = true)
    protected String guid;
    @XmlAttribute(name = "ds_code", required = true)
    protected int dsCode;
    @XmlAttribute(name = "dc_seria", required = false)
    protected String dcSeria;
    @XmlAttribute(name = "dc_number", required = false)
    @XmlSchemaType(name = "unsignedInt")
    protected Integer dcNumber;
    @XmlAttribute(name = "dateto", required = true)
    protected String dateto;
    @XmlAttribute(name = "method_izm")
    @XmlSchemaType(name = "unsignedInt")
    protected Byte methodIzm;
    @XmlAttribute(name = "reg_number", required = true)
    protected String regNumber;
    @XmlAttribute(name = "dateauto", required = true)
    protected short dateauto;
    @XmlAttribute(name = "tc_seria", required = false)
    protected String tcSeria;
    @XmlAttribute(name = "tc_number", required = false)
    protected Integer tcNumber;
    @XmlAttribute(name = "vin_is_chassi", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short vinIsChassi;
    @XmlAttribute(name = "vin")
    protected String vin;
    @XmlAttribute(name = "id_ts_type", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short idTsType;
    @XmlAttribute(name = "id_ts_type_engine", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short idTsTypeEngine;
    @XmlAttribute(name = "model_id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected int modelId;
    @XmlAttribute(name = "id_ts_categ", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected byte idTsCateg;
    @XmlAttribute(name = "id_ts_use", required = false)
    @XmlSchemaType(name = "unsignedByte")
    protected Byte idTsUse;
    @XmlAttribute(name = "id_ts_colour", required = true)
    @XmlSchemaType(name = "unsignedShort")
    protected int idTsColour;
    @XmlAttribute(name = "eko_class")
    protected Byte ekoClass;
    @XmlAttribute(name = "weight", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected int weight;
    @XmlAttribute(name = "mileage", required = true)
    protected Integer mileage;
    @XmlAttribute(name = "parent")
    protected String parent;
    @XmlAttribute(name = "dl", required = true)
    protected String dl;
    @XmlAttribute(name = "num_inspection")
    @XmlSchemaType(name = "unsignedByte")
    protected byte numInspection;
    @XmlAttribute(name = "check")
    protected String check;
    @XmlAttribute(name = "notes")
    protected String notes;
    @XmlAttribute(name = "mismatch")
    protected String mismatch;
    @XmlAttribute(name = "photo")
    @XmlSchemaType(name = "unsignedInt")
    protected Long photo;
    @XmlAttribute(name = "conclusion", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected byte conclusion;
    @XmlAttribute(name = "owner_name")
    protected String ownerName;
    @XmlAttribute(name = "owner_type")
    protected String ownerType;
    @XmlAttribute(name = "owner_unp")
    protected String ownerUnp;
    @XmlAttribute(name = "customer_name")
    protected String customerName;
    @XmlAttribute(name = "customer_type")
    protected String customerType;
    @XmlAttribute(name = "customer_unp")
    protected String customerUnp;

    @XmlAttribute(name = "symma_s_nds")
    @XmlJavaTypeAdapter(BigDecimalXmlAdapter.class)
    protected BigDecimal symmaSNds;

    @XmlAttribute(name = "symma_nds")
    @XmlJavaTypeAdapter(BigDecimalXmlAdapter.class)
    protected BigDecimal symmaNds;

    @XmlAttribute(name = "blanc_repeat_date")
    protected String blancRepeatDate;
    @XmlAttribute(name = "blanc_repeat_seria")
    protected String blancRepeatSeria;
    @XmlAttribute(name = "blanc_repeat_number")
    protected String blancRepeatNumber;
    @XmlAttribute(name = "user_name")
    protected String userName;

    public Long getIdDsoDiagnCard() {
        return idDsoDiagnCard;
    }

    public void setIdDsoDiagnCard(Long value) {
        this.idDsoDiagnCard = value;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String value) {
        this.guid = value;
    }

    public int getDsCode() {
        return dsCode;
    }

    public void setDsCode(int value) {
        this.dsCode = value;
    }

    public String getDcSeria() {
        return dcSeria;
    }

    public void setDcSeria(String value) {
        this.dcSeria = value;
    }

    public Integer getDcNumber() {
        return dcNumber;
    }

    public void setDcNumber(Integer value) {
        this.dcNumber = value;
    }

    public String getDateto() {
        return dateto;
    }

    public void setDateto(String value) {
        this.dateto = value;
    }

    public Byte getMethodIzm() {
        return methodIzm;
    }

    public void setMethodIzm(Byte value) {
        this.methodIzm = value;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String value) {
        this.regNumber = value;
    }

    public short getDateauto() {
        return dateauto;
    }

    public void setDateauto(short value) {
        this.dateauto = value;
    }

    public String getTcSeria() {
        return tcSeria;
    }

    public void setTcSeria(String value) {
        this.tcSeria = value;
    }

    public Integer getTcNumber() {
        return tcNumber;
    }

    public void setTcNumber(Integer value) {
        this.tcNumber = value;
    }

    public short getVinIsChassi() {
        return vinIsChassi;
    }

    public void setVinIsChassi(short value) {
        this.vinIsChassi = value;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String value) {
        this.vin = value;
    }

    public short getIdTsType() {
        return idTsType;
    }

    public void setIdTsType(short value) {
        this.idTsType = value;
    }

    public short getIdTsTypeEngine() {
        return idTsTypeEngine;
    }

    public void setIdTsTypeEngine(short value) {
        this.idTsTypeEngine = value;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public byte getIdTsCateg() {
        return idTsCateg;
    }

    public void setIdTsCateg(byte value) {
        this.idTsCateg = value;
    }

    public Byte getIdTsUse() {
        return idTsUse;
    }

    public void setIdTsUse(Byte value) {
        this.idTsUse = value;
    }

    public int getIdTsColour() {
        return idTsColour;
    }

    public void setIdTsColour(int value) {
        this.idTsColour = value;
    }

    public Byte getEkoClass() {
        return ekoClass;
    }

    public void setEkoClass(Byte value) {
        this.ekoClass = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int value) {
        this.weight = value;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer value) {
        this.mileage = value;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String value) {
        this.parent = value;
    }

    public String getDl() {
        return dl;
    }

    public void setDl(String value) {
        this.dl = value;
    }

    public byte getNumInspection() {
        return numInspection;
    }

    public void setNumInspection(byte value) {
        this.numInspection = value;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String value) {
        this.check = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        this.notes = value;
    }

    public String getMismatch() {
        return mismatch;
    }

    public void setMismatch(String value) {
        this.mismatch = value;
    }

    public Long getPhoto() {
        return photo;
    }

    public void setPhoto(Long value) {
        this.photo = value;
    }

    public byte getConclusion() {
        return conclusion;
    }

    public void setConclusion(byte value) {
        this.conclusion = value;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String value) {
        this.ownerName = value;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String value) {
        this.ownerType = value;
    }

    public String getOwnerUnp() {
        return ownerUnp;
    }

    public void setOwnerUnp(String value) {
        this.ownerUnp = value;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String value) {
        this.customerName = value;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String value) {
        this.customerType = value;
    }

    public String getCustomerUnp() {
        return customerUnp;
    }

    public void setCustomerUnp(String value) {
        this.customerUnp = value;
    }

    public BigDecimal getSymmaSNds() {
        return symmaSNds;
    }

    public void setSymmaSNds(BigDecimal value) {
        this.symmaSNds = value;
    }

    public BigDecimal getSymmaNds() {
        return symmaNds;
    }

    public void setSymmaNds(BigDecimal value) {
        this.symmaNds = value;
    }

    public String getBlancRepeatDate() {
        return blancRepeatDate;
    }

    public void setBlancRepeatDate(String value) {
        this.blancRepeatDate = value;
    }

    public String getBlancRepeatSeria() {
        return blancRepeatSeria;
    }

    public void setBlancRepeatSeria(String value) {
        this.blancRepeatSeria = value;
    }

    public String getBlancRepeatNumber() {
        return blancRepeatNumber;
    }

    public void setBlancRepeatNumber(String value) {
        this.blancRepeatNumber = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    @Override
    public long getId() {
        return getIdDsoDiagnCard();
    }
    @Override
    public String toString() {
        return "ResGTOXmlV8.Doc{ guid=" + guid + ", idDsoDiagnCard=" + idDsoDiagnCard + ", dcSeria=" + dcSeria +
                ", dcNumber=" + dcNumber + ", dateto=" + dateto + ", userName=" + userName +
                ", regNumber=" + regNumber + ", dateauto=" + dateauto + ", mileage=" + mileage +
                ", ownerName=" + ownerName + ", ownerUnp=" + ownerUnp + ", vin=" + vin +
                ", methodIzm=" + methodIzm + ", symmaNds=" + symmaNds +
                ", symmaSNds=" + symmaSNds + ", numInspection=" + numInspection +
                ", conclusion=" + conclusion + ", photo=" + photo + ", check=" + check +
                ", notes=" + notes + ", mismatch=" + mismatch + ", parent=" + parent +
                ", ownerType=" + ownerType +
                ", dl=" + dl + ", idTsType=" + idTsType +
                ", customerName=" + customerName + ", customerUnp=" + customerUnp +
                ", vinIsChassi=" + vinIsChassi + ", dsCode=" + dsCode + ", idTsTypeEngine=" + idTsTypeEngine +
                ", idTsCateg=" + idTsCateg + ", modelId=" + modelId +
                ", idTsColour=" + idTsColour + '}';
    }
}
