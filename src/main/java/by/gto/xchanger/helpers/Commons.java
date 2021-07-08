package by.gto.xchanger.helpers;

import by.gto.library.entity.RegNumberParseResult;
import by.gto.library.helpers.BlankNumberHelpers;
import by.gto.library.helpers.GuidHelpers;
import by.gto.xchanger.model.EntityDescriptor;
import by.gto.xml.entities.DiagCard;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class Commons {
    /**
     * GUID центрального участника обмена.
     */
    public static final byte[] OWN_GUID = GuidHelpers.guidAsBytes("543E004D-8754-4D1D-97A9-F84D9F3BB00D");
    private static final BigDecimal MAX_TI_TOTAL = new BigDecimal("9999.99");
    private static final BigDecimal MAX_TI_VAT = new BigDecimal("999.99");

    public static Map<String, Object> fillDiagCardParams(DiagCard dc, boolean loadedByProtocol) {
        RegNumberParseResult regNumberParseResult = dc.getRegNumberParseResult();
        Map<String, Object> result = new HashMap<>();
        result.put("IDFIRM", dc.getIDFIRM());
        result.put("firm_code", dc.getFirmCode());
        result.put("IDMODELSTC", dc.getIDMODELSTC());
        result.put("gai_model_id", dc.getGaiModelId());
        result.put("IDTYPETC", dc.getIDTYPETC());
        result.put("IDTYPEENGINESTC", dc.getIDTYPEENGINESTC());
        result.put("IDCOLORSTC", dc.getIDCOLORSTC());
        //result.put("SERIAL", "Й");

        result.put("SERIAL", StringUtils.substring(dc.getSERIAL(), 0, 2));
        result.put("GNUMBER", StringUtils.substring(regNumberParseResult.getLetters(), 0, 10));
        result.put("DATEAUTO", dc.getDATEAUTO());
        result.put("ONECHECK", dc.getONECHECK());
        result.put("SECONDCHECK", dc.getSECONDCHECK());
        //String vin = StringUtils.substring(StringUtils.trim(dc.getVINAUTO()), 0, 30);
        result.put("REVERSEVIN", StringUtils.substring(dc.getREVERSEVIN(), 0, 18));
        result.put("VIN", StringUtils.substring(dc.getVINAUTO(), 0, 18));
        result.put("SHNUMBER", StringUtils.substring(dc.getSHNUMBER(), 0, 30));
        result.put("OWNER", StringUtils.substring(dc.getOWNER(), 0, 150));
        result.put("PARENT", StringUtils.substring(dc.getPARENT(), 0, 150));
        result.put("SPSERIAL", StringUtils.substring(dc.getSPSERIAL(), 0, 4));
        result.put("CONDITIONTC", dc.getCONDITIONTC());
        result.put("DATETO", Timestamp.valueOf(dc.getDATETO()));
        result.put("URLICO", dc.getURLICO());
        result.put("EMPLOYEE", StringUtils.substring(dc.getEMPLOYEE(), 0, 70));
        result.put("TRANSINPECTIONDATA_ID", dc.getTRANSINPECTIONDATA_ID());
        result.put("GNUMBERDIGITS", regNumberParseResult.getDigits());
        result.put("VER", dc.getVER().shortValue());

        result.put("DP1", dc.getDP1());
        result.put("DP2", dc.getDP2());
        result.put("DP3", dc.getDP3());
        result.put("DP4", dc.getDP4());
        result.put("DP5", dc.getDP5());
        result.put("DP6", dc.getDP6());
        result.put("DP7", dc.getDP7());
        result.put("DP8", dc.getDP8());
        result.put("DP9", dc.getDP9());
        result.put("DP10", dc.getDP10());
        result.put("DP11", dc.getDP11());
        result.put("DP12", dc.getDP12());
        result.put("DP13", dc.getDP13());
        result.put("DP14", dc.getDP14());
        result.put("DP15", dc.getDP15());
        result.put("DP16", dc.getDP16());
        result.put("DP17", dc.getDP17());
        result.put("DP18", dc.getDP18());
        result.put("DP19", dc.getDP19());
        result.put("DP20", dc.getDP20());
        result.put("DP21", dc.getDP21());
        result.put("DP22", dc.getDP22());
        result.put("DP23", dc.getDP23());
        result.put("DP24", dc.getDP24());
        result.put("DP25", dc.getDP25());
        result.put("DP26", dc.getDP26());
        result.put("DP27", dc.getDP27());
        result.put("DP28", dc.getDP28());
        result.put("DP29", dc.getDP29());
        result.put("DP30", dc.getDP30());
        result.put("DP31", dc.getDP31());
        result.put("DP32", dc.getDP32());
        result.put("DP33", dc.getDP33());
        result.put("DP34", dc.getDP34());
        result.put("DP35", dc.getDP35());
        result.put("DP36", dc.getDP36());
        result.put("DP37", dc.getDP37());
        result.put("DP38", dc.getDP38());
        result.put("DP39", dc.getDP39());
        result.put("DP40", dc.getDP40());
        result.put("DP41", dc.getDP41());
        result.put("DP42", dc.getDP42());
        result.put("DP43", dc.getDP43());
        result.put("DP44", dc.getDP44());
        result.put("DP45", dc.getDP45());
        result.put("DP46", dc.getDP46());
        result.put("DP47", dc.getDP47());
        result.put("DP48", dc.getDP48());
        result.put("DP49", dc.getDP49());
        result.put("DP50", dc.getDP50());
        result.put("DP51", dc.getDP51());
        result.put("DP52", dc.getDP52());
        result.put("DP53", dc.getDP53());
        result.put("DP54", dc.getDP54());
        result.put("DP55", dc.getDP55());
        result.put("DP56", dc.getDP56());
        result.put("DP57", dc.getDP57());
        result.put("DP58", dc.getDP58());

        result.put("SERIALNUMBER", dc.getSERIALNUMBER());
        result.put("SPNUMBER", dc.getSPNUMBER());

        result.put("POSSIBLY_WRONG_CONDITION", dc.getPOSSIBLY_WRONG_CONDITION().shortValue());
        result.put("guid", GuidHelpers.guidAsBytes(UUID.fromString(dc.getGuid())));
        String dlSeries = dc.getDL_SERIES();
        int dlNumber = dc.getDL_NUMBER();
        if(dlNumber == 0 || StringUtils.isBlank(dlSeries)) {
            result.put("DL", null);
            result.put("DL_DIGITS", null);
        } else {
            result.put("DL", BlankNumberHelpers.normalizeDriversLicense(
                    StringUtils.substring(StringUtils.trim(dlSeries), 0, 5) +
                            StringUtils.leftPad(String.valueOf(dlNumber), 7, '0')
            ));
            result.put("DL_DIGITS", dlNumber);
        }
        result.put("KILOMETRAGE", dc.getKILOMETRAGE());
        result.put("WEIGHT", dc.getWEIGHT());
        result.put("MEASUREMENT_METHOD", dc.getMEASUREMENT_METHOD());
        result.put("ECOLOGICAL_CLASS", dc.getECOLOGICAL_CLASS());
        result.put("LOADED_BY_PROTOCOL", (short) (loadedByProtocol ? 1 : 0));
        result.put("CATEGORY_ID", dc.getCATEGORY_ID());
        result.put("APPLYING", dc.getApplying());

        result.put("employee_id", dc.getEmployeeId());
        result.put("vehicle_id", dc.getVehicleId());
        result.put("reg_number1_id", dc.getRegNumberId());
        result.put("owner_id", dc.getOwnerId());
        result.put("holder_id", dc.getHolderId());
        result.put("customer_id", dc.getCustomerId());
        result.put("checks_packed", dc.getChecksPacked());
        result.put("guid_bytes", dc.getGuidBytes());
        result.put("normalized_rn", dc.getNormalizedRegNumber());
        result.put("total", (dc.getTotal() == null) ? null : MAX_TI_TOTAL.min(dc.getTotal()));
        result.put("vat", (dc.getVat() == null) ? null : MAX_TI_VAT.min(dc.getVat()));
        return result;
    }

    public static String getChangesQuery(EntityDescriptor entityDescriptor) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("SELECT t.").append(entityDescriptor.getIdFieldName()).append(", cast(t.valid as int) valid");
        for (Map.Entry<String, String> pair : entityDescriptor.getFieldsToExport().entrySet()) {
            sb.append(", t.").append(pair.getKey());
        }
        sb.append("\nFROM changes_registry INNER JOIN ").append(entityDescriptor.getTableName()).append(" t \n")
            .append("ON (t.").append(entityDescriptor.getIdFieldName()).append(" = changes_registry.entity_id_int32)\n")
            .append("WHERE changes_registry.name = :entity_name\n"
                + "        AND changes_registry.peer_id = :peer_id\n"
                + "        AND changes_registry.deletion = 0\n"
                + "        AND changes_registry.start_date <= :export_date\n"
                + "        AND t.not_for_export = 0");
        return sb.toString();
    }

    public static List<Integer> getDeletions(
        String entityName,
        byte[] peerId,
        NamedParameterJdbcTemplate jdbcTemplate,
        Date exportDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("entity_name", entityName);
        params.put("export_date", exportDate);

        List<Integer> result = jdbcTemplate.query(
            "SELECT entity_id_int32 FROM changes_registry WHERE name=:entity_name AND peer_id = :peer_id "
                + "AND deletion = 1 and start_date <= :export_date",
            params, (rs, rowNum) -> rs.getInt(1));
        return result;
    }

    public static List<Map<String, Object>> getChanges(
        EntityDescriptor entityDescriptor,
        byte[] peerId,
        NamedParameterJdbcTemplate jdbcTemplate,
        Date exportDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("export_date", exportDate);
        params.put("entity_name", entityDescriptor.getEntityName());

        List<Map<String, Object>> result = jdbcTemplate.query(
            getChangesQuery(entityDescriptor),
            params, (rs, rowNum) -> {
                Map<String, Object> change = new HashMap<>(2);
                change.put(entityDescriptor.getIdFieldName(), rs.getInt(entityDescriptor.getIdFieldName()));
                change.put("valid", rs.getShort("valid"));
                for (Map.Entry<String, String> pair : entityDescriptor.getFieldsToExport().entrySet()) {
                    change.put(pair.getValue(), rs.getObject(pair.getKey()));
                }
                return change;
            });
        return result;
    }
}
