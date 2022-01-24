package model.xml;

import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BigDecimalXmlAdapter extends XmlAdapter<String, BigDecimal> {
    @Override
    public BigDecimal unmarshal(String v) throws Exception {
        BigDecimal r = new BigDecimal(v.replace(',', '.'));
        return r;
    }

    @Override
    public String marshal(BigDecimal v) throws Exception {
        String r = v.toString();
        return r;
    }
}
