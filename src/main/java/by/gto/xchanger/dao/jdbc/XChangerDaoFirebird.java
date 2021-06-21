package by.gto.xchanger.dao.jdbc;

import by.gto.xchanger.dao.XChangerDao;
import by.gto.xchanger.helpers.Commons;
import by.gto.xchanger.model.EntityDescriptor;
import by.gto.xml.entities.DiagCard;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class XChangerDaoFirebird implements XChangerDao {
    protected NamedParameterJdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Integer> getDeletions(String entityName, byte[] peerId, Date exportDate) {
        return Commons.getDeletions(entityName, peerId, jdbcTemplate, exportDate);
    }

    @Override
    public List<Map<String, Object>> getChanges(EntityDescriptor entityDescriptor, byte[] peerId, Date exportDate) {
        return Commons.getChanges(entityDescriptor, peerId, jdbcTemplate, exportDate);
    }


    @Override
    public void updateChangesRegistry(byte[] senderId, int msgNumber, Date exportDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", senderId);
        params.put("message_number", msgNumber);
        jdbcTemplate.update("UPDATE changes_registry SET message_number=:message_number WHERE peer_id=:peer_id AND message_number IS NULL", params);
    }

    @Override
    public void registerReferenceForPeer(byte[] peerId, EntityDescriptor entityDescriptor) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("entity_name", entityDescriptor.getEntityName());

        jdbcTemplate.update("DELETE FROM changes_registry " +
                "WHERE changes_registry.name = :entity_name " +
                "AND changes_registry.peer_id = :peer_id", params);
        String query = String.format("insert into changes_registry (peer_id, name, entity_id_int32, deletion)\n" +
                "select :peer_id, :entity_name, %1$s.%2$s, 0 from %1$s", entityDescriptor.getTableName(), entityDescriptor.getIdFieldName());
        jdbcTemplate.update(query, params);
    }

    @Override
    public int createPeer(byte[] peerId, int msgFormatVersion) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("format_version", msgFormatVersion);
        return jdbcTemplate.update("INSERT INTO peers (peer_id, format_version) VALUES (:peer_id, :format_version)", params);
    }

    @Override
    public int updateRegistry(byte[] peerId, int successfullyReceivedMessageNumber) {
        String sql = "DELETE FROM changes_registry WHERE peer_id = :peer_id AND message_number <= :message_number";
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("message_number", successfullyReceivedMessageNumber);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int updateMessageNumberInPeerTable(byte[] peerId, int msgNumber, Long dsCode, int msgFormatVersion) {
        String sql = "update peers set peers.peer_active = 1, " +
                "peers.format_version = :msg_format_version, " +
                "peers.last_received_message = :last_received_message, " +
                "peers.last_received_time = :last_received_time " +
                ((dsCode != null) ? ", peers.ds_code = :ds_code" : "") +
                " where peers.peer_id=:peer_id";
        Map<String, Object> params = new HashMap<>();
        params.put("last_received_message", msgNumber);
        params.put("last_received_time", new Date());
        if (dsCode != null) {
            params.put("ds_code", dsCode.intValue());
        }
        params.put("peer_id", peerId);
        params.put("msg_format_version", msgFormatVersion);
        return jdbcTemplate.update(sql, params);
    }


    @Override
    public Map<Integer, Integer> getCodesToIdsMaping() {
        final HashMap<Integer, Integer> result = new HashMap<>();
        jdbcTemplate.query("select code, id from firms", resultSet -> {
            result.put(resultSet.getInt(1), resultSet.getInt(2));
        });

        return result;
    }

    @Override
    public int[] getChecksMapping() {
        final HashMap<Integer, Integer> map = new HashMap<>();
        jdbcTemplate.query("select max(id_note) from note", resultSet -> {
            map.put(-100, resultSet.getInt(1));
        });

        if (!map.containsKey(-100)) {
            return null;
        }

        int max = map.get(-100);
        final int[] result = new int[max + 1];

        for (int i = 1; i <= max; i++) {
            result[i] = 0;
        }

        jdbcTemplate.query("select id_note, id_param from note order by id_note", resultSet -> {
            result[resultSet.getInt(1)] = resultSet.getInt(2);
        });
        return result;
    }

    @Override
    public byte[] getMyGuid() {
        final List<byte[]> result = new ArrayList<>();
        jdbcTemplate.query("select first 1 peer_Id from peers where me <> 0", resultSet -> {
            byte[] bytes = resultSet.getBytes(1);
            result.add(bytes);
        });
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public List<Integer> getMessageNumbers(byte[] peerId) {
        final List<Integer> r = new ArrayList<>();
        String sqlQuery = "SELECT COALESCE(peers.last_sent_message, 0), COALESCE(LAST_RECEIVED_MESSAGE, 0), FORMAT_VERSION FROM peers WHERE peer_id = :peer_id";
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        jdbcTemplate.query(sqlQuery, params,
                resultSet -> {
                    if (r.size() == 0) {
                        r.add(resultSet.getInt(1));
                        r.add(resultSet.getInt(2));
                        r.add(resultSet.getInt(3));
                    }
                });
        return r;
    }

    @Override
    public void updateLastSentMessage(byte[] peerId, int lastSentMessage) {
        Map<String, Object> params = new HashMap<>();
        params.put("peerId", peerId);
        params.put("last_sent_message", lastSentMessage);
        jdbcTemplate.update("UPDATE peers SET last_sent_message = :last_sent_message, last_sent_time = cast('now' AS TIMESTAMP) WHERE peer_id = :peerId", params);
    }




    @Override
    public Integer getReceivedMessageNumber(byte[] peerId) {
        final List<Integer> r = new ArrayList<>();
        String sqlQuery = "SELECT coalesce(peers.last_received_message, -1), PEER_ACTIVE FROM peers WHERE peers.peer_id = :peerId";
        Map<String, Object> params = new HashMap<>();
        params.put("peerId", peerId);
        jdbcTemplate.query(sqlQuery, params,
                resultSet -> {
                    if (resultSet.getInt(2) == 0) {
                        r.add(-2);
                    } else {
                        r.add(resultSet.getInt(1));
                    }
                });
        if (r.size() == 0) {
            return null;
        } else {
            return r.get(0);
        }
    }

    @Override
    public int deletePti(byte[] toDel, LocalDate minimumAllowedDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("guid", toDel);
        params.put("minimumAllowedDate", minimumAllowedDate);

        return jdbcTemplate.update("DELETE FROM maps WHERE guid=:guid and dateto >= :minimumAllowedDate", params);
    }

//    @Override
//    public int insertPTI(DiagCard dc, XchangeOptions options) {
//        String sql = "INSERT INTO MAPS "
//                + "( IDFIRM,  IDMODELSTC,  IDTYPETC,  IDTYPEENGINESTC,  IDCOLORSTC,  SERIAL,  GNUMBER,  DATEAUTO,  ONECHECK,  SECONDCHECK,   SHNUMBER,  OWNER,  PARENT,  SPSERIAL,  CONDITIONTC,  DATETO,   URLICO,  EMPLOYEE,  TRANSINPECTIONDATA_ID,  GNUMBERDIGITS,  VER,  DP1,  DP2,  DP3,  DP4,  DP5,  DP6,  DP7,  DP8,  DP9,  DP10,  DP11,  DP12,  DP13,  DP14,  DP15,  DP16,  DP17,  DP18,  DP19,  DP20,  DP21,  DP22,  DP23,  DP24,  DP25,  DP26,  DP27,  DP28,  DP29,  DP30,  DP31,  DP32,  DP33,  DP34,  DP35,  DP36,  DP37,  DP38,  DP39,  DP40,  DP41,  DP42,  DP43,  DP44,  DP45,  DP46,  DP47,  DP48,  DP49,  DP50,  DP51,  DP52,  DP53,  DP54,  DP55,  DP56,  DP57,  SERIALNUMBER,  SPNUMBER,  REVERSEVIN,  VIN,  POSSIBLY_WRONG_CONDITION,  DP58,   guid,  DL_SERIES,  DL_NUMBER,  KILOMETRAGE,  WEIGHT,  MEASUREMENT_METHOD,  ECOLOGICAL_CLASS,  LOADED_BY_PROTOCOL,  CATEGORY_ID,  APPLYING) "
//                + "VALUES "
//                + "(:IDFIRM, :IDMODELSTC, :IDTYPETC, :IDTYPEENGINESTC, :IDCOLORSTC, :SERIAL, :GNUMBER, :DATEAUTO, :ONECHECK, :SECONDCHECK,  :SHNUMBER, :OWNER, :PARENT, :SPSERIAL, :CONDITIONTC, :DATETO,  :URLICO, :EMPLOYEE, :TRANSINPECTIONDATA_ID, :GNUMBERDIGITS, :VER, :DP1, :DP2, :DP3, :DP4, :DP5, :DP6, :DP7, :DP8, :DP9, :DP10, :DP11, :DP12, :DP13, :DP14, :DP15, :DP16, :DP17, :DP18, :DP19, :DP20, :DP21, :DP22, :DP23, :DP24, :DP25, :DP26, :DP27, :DP28, :DP29, :DP30, :DP31, :DP32, :DP33, :DP34, :DP35, :DP36, :DP37, :DP38, :DP39, :DP40, :DP41, :DP42, :DP43, :DP44, :DP45, :DP46, :DP47, :DP48, :DP49, :DP50, :DP51, :DP52, :DP53, :DP54, :DP55, :DP56, :DP57, :SERIALNUMBER, :SPNUMBER, :REVERSEVIN, :VIN, :POSSIBLY_WRONG_CONDITION, :DP58,  :guid, :DL_SERIES, :DL_NUMBER, :KILOMETRAGE, :WEIGHT, :MEASUREMENT_METHOD, :ECOLOGICAL_CLASS, :LOADED_BY_PROTOCOL, :CATEGORY_ID, :APPLYING)";
//        return jdbcTemplate.update(sql, fillDiagCardParams(dc, options.isOnlyLoadData()));
//    }

//    @Override
//    public int updatePTI(DiagCard dc, boolean onlyLoadData) {
//        String sql = "UPDATE maps SET "
//                + "IDFIRM = :IDFIRM, "
//                + "IDMODELSTC = :IDMODELSTC, "
//                + "IDTYPETC = :IDTYPETC, "
//                + "IDTYPEENGINESTC = :IDTYPEENGINESTC, "
//                + "IDCOLORSTC = :IDCOLORSTC, "
//                + "SERIAL = :SERIAL, "
//                + "GNUMBER = :GNUMBER, "
//                + "DATEAUTO = :DATEAUTO, "
//                + "ONECHECK = :ONECHECK, "
//                + "SECONDCHECK = :SECONDCHECK, "
//                + "SHNUMBER = :SHNUMBER, "
//                + "OWNER = :OWNER, "
//                + "PARENT = :PARENT, "
//                + "SPSERIAL = :SPSERIAL, "
//                + "CONDITIONTC = :CONDITIONTC, "
//                + "DATETO = :DATETO, "
//                + "URLICO = :URLICO, "
//                + "EMPLOYEE = :EMPLOYEE, "
//                + "TRANSINPECTIONDATA_ID = :TRANSINPECTIONDATA_ID, "
//                + "GNUMBERDIGITS = :GNUMBERDIGITS, "
//                + "VER = :VER, "
//                + "DP1 = :DP1, "
//                + "DP2 = :DP2, "
//                + "DP3 = :DP3, "
//                + "DP4 = :DP4, "
//                + "DP5 = :DP5, "
//                + "DP6 = :DP6, "
//                + "DP7 = :DP7, "
//                + "DP8 = :DP8, "
//                + "DP9 = :DP9, "
//                + "DP10 = :DP10, "
//                + "DP11 = :DP11, "
//                + "DP12 = :DP12, "
//                + "DP13 = :DP13, "
//                + "DP14 = :DP14, "
//                + "DP15 = :DP15, "
//                + "DP16 = :DP16, "
//                + "DP17 = :DP17, "
//                + "DP18 = :DP18, "
//                + "DP19 = :DP19, "
//                + "DP20 = :DP20, "
//                + "DP21 = :DP21, "
//                + "DP22 = :DP22, "
//                + "DP23 = :DP23, "
//                + "DP24 = :DP24, "
//                + "DP25 = :DP25, "
//                + "DP26 = :DP26, "
//                + "DP27 = :DP27, "
//                + "DP28 = :DP28, "
//                + "DP29 = :DP29, "
//                + "DP30 = :DP30, "
//                + "DP31 = :DP31, "
//                + "DP32 = :DP32, "
//                + "DP33 = :DP33, "
//                + "DP34 = :DP34, "
//                + "DP35 = :DP35, "
//                + "DP36 = :DP36, "
//                + "DP37 = :DP37, "
//                + "DP38 = :DP38, "
//                + "DP39 = :DP39, "
//                + "DP40 = :DP40, "
//                + "DP41 = :DP41, "
//                + "DP42 = :DP42, "
//                + "DP43 = :DP43, "
//                + "DP44 = :DP44, "
//                + "DP45 = :DP45, "
//                + "DP46 = :DP46, "
//                + "DP47 = :DP47, "
//                + "DP48 = :DP48, "
//                + "DP49 = :DP49, "
//                + "DP50 = :DP50, "
//                + "DP51 = :DP51, "
//                + "DP52 = :DP52, "
//                + "DP53 = :DP53, "
//                + "DP54 = :DP54, "
//                + "DP55 = :DP55, "
//                + "DP56 = :DP56, "
//                + "DP57 = :DP57, "
//                + "SERIALNUMBER = :SERIALNUMBER, "
//                + "SPNUMBER = :SPNUMBER, "
//                + "REVERSEVIN = :REVERSEVIN, "
//                + "VIN = :VIN, "
//                + "POSSIBLY_WRONG_CONDITION = :POSSIBLY_WRONG_CONDITION, "
//                + "DP58 = :DP58, "
//                + "DL_SERIES = :DL_SERIES, "
//                + "DL_NUMBER = :DL_NUMBER, "
//                + "KILOMETRAGE = :KILOMETRAGE, "
//                + "WEIGHT = :WEIGHT, "
//                + "MEASUREMENT_METHOD = :MEASUREMENT_METHOD, "
//                + "ECOLOGICAL_CLASS = :ECOLOGICAL_CLASS, "
//                + "LOADED_BY_PROTOCOL = :LOADED_BY_PROTOCOL, "
//                + "CATEGORY_ID = :CATEGORY_ID, "
//                + "APPLYING = :APPLYING "
//
//                + "WHERE guid = :guid";
//        return jdbcTemplate.update(sql, fillDiagCardParams(dc, onlyLoadData));
//    }

    @Override
    public int insertOrUpdatePTI(DiagCard dc, boolean loadedByProtocol) {
        String sql = "UPDATE or INSERT INTO MAPS \n"
                + "( IDFIRM,  IDMODELSTC,  IDTYPETC,  IDTYPEENGINESTC,  IDCOLORSTC,  SERIAL,  GNUMBER,  DATEAUTO,  ONECHECK,  SECONDCHECK,   SHNUMBER,  OWNER,  PARENT,  SPSERIAL,  CONDITIONTC,  DATETO,  URLICO,  EMPLOYEE,  TRANSINPECTIONDATA_ID,  GNUMBERDIGITS,  VER,  DP1,  DP2,  DP3,  DP4,  DP5,  DP6,  DP7,  DP8,  DP9,  DP10,  DP11,  DP12,  DP13,  DP14,  DP15,  DP16,  DP17,  DP18,  DP19,  DP20,  DP21,  DP22,  DP23,  DP24,  DP25,  DP26,  DP27,  DP28,  DP29,  DP30,  DP31,  DP32,  DP33,  DP34,  DP35,  DP36,  DP37,  DP38,  DP39,  DP40,  DP41,  DP42,  DP43,  DP44,  DP45,  DP46,  DP47,  DP48,  DP49,  DP50,  DP51,  DP52,  DP53,  DP54,  DP55,  DP56,  DP57,  SERIALNUMBER,  SPNUMBER,  REVERSEVIN,  VIN,  POSSIBLY_WRONG_CONDITION,  DP58,   guid,  DL_SERIES,  DL_NUMBER,  KILOMETRAGE,  WEIGHT,  MEASUREMENT_METHOD,  ECOLOGICAL_CLASS,  LOADED_BY_PROTOCOL,  CATEGORY_ID,  APPLYING) \n"
                + "VALUES "
                + "(:IDFIRM, :IDMODELSTC, :IDTYPETC, :IDTYPEENGINESTC, :IDCOLORSTC, :SERIAL, :GNUMBER, :DATEAUTO, :ONECHECK, :SECONDCHECK,  :SHNUMBER, :OWNER, :PARENT, :SPSERIAL, :CONDITIONTC, :DATETO, :URLICO, :EMPLOYEE, :TRANSINPECTIONDATA_ID, :GNUMBERDIGITS, :VER, :DP1, :DP2, :DP3, :DP4, :DP5, :DP6, :DP7, :DP8, :DP9, :DP10, :DP11, :DP12, :DP13, :DP14, :DP15, :DP16, :DP17, :DP18, :DP19, :DP20, :DP21, :DP22, :DP23, :DP24, :DP25, :DP26, :DP27, :DP28, :DP29, :DP30, :DP31, :DP32, :DP33, :DP34, :DP35, :DP36, :DP37, :DP38, :DP39, :DP40, :DP41, :DP42, :DP43, :DP44, :DP45, :DP46, :DP47, :DP48, :DP49, :DP50, :DP51, :DP52, :DP53, :DP54, :DP55, :DP56, :DP57, :SERIALNUMBER, :SPNUMBER, :REVERSEVIN, :VIN, :POSSIBLY_WRONG_CONDITION, :DP58,  :guid, :DL_SERIES, :DL_NUMBER, :KILOMETRAGE, :WEIGHT, :MEASUREMENT_METHOD, :ECOLOGICAL_CLASS, :LOADED_BY_PROTOCOL, :CATEGORY_ID, :APPLYING) \n" +
                "MATCHING (GUID)";

        return jdbcTemplate.update(sql, Commons.fillDiagCardParams(dc, loadedByProtocol));
    }

    @Override
    public List<byte[]> findAdditionalPeers(Date date) {
        final List<byte[]> r = new ArrayList<>();
        String sql = "select distinct changes_registry.peer_id as idd \n"
                + "from changes_registry left join peers on (peers.peer_id = changes_registry.peer_id) \n"
                + "where changes_registry.MESSAGE_NUMBER is null and peers.peer_active <>0 and peers.me = 0 \n"
                + "union \n"
                + "select peers.peer_id \n"
                + "from peers \n"
                + "left join changes_registry on (peers.peer_id = changes_registry.peer_id) \n"
                + "where peers.peer_active <> 0 and ((peers.last_sent_time is null) \n"
                + "or (cast('NOW' as timestamp) - peers.last_sent_time) > 1) \n"
                + "group by peers.peer_id \n"
                + "having count(changes_registry.peer_id) > 0";
        jdbcTemplate.query(sql, resultSet -> {
            r.add(resultSet.getBytes(1));
        });
        return r;
    }

    @Override
    public String getDBVersion() {
        final String result[] = new String[]{"_err_"};
        jdbcTemplate.query("SELECT first 1 features, structure FROM versions", (Map) null, resultSet -> {
            result[0] = "" + resultSet.getInt(1) + "." + resultSet.getInt(2);
        });
        return result[0];
    }

    @Override
    public String test() {
        return null;
    }

    @Override
    public Integer getLock() {
        return 1;
    }

    @Override
    public Integer releaseLock() {
        return null;
    }

    @Override
    public int inactivateOldPeers(long days) {
        LocalDateTime.now().minusDays(days);
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDateTime.now().minusDays(Math.max(days, 1L)));
        return jdbcTemplate.update("UPDATE peers SET peer_active = 0 WHERE peer_active = 1 AND last_received_time < :date AND me <> 1", params);
    }

    @Override
    public Map<Integer, Integer> getMapOurModelIdToGaiModelId() {
        Map<Integer, Integer> r = new HashMap<>(10000);
        jdbcTemplate.query("SELECT id, idgai FROM modelstc", rs -> {
            r.put(rs.getInt(1), rs.getInt(2));
        });
        return r;
    }

    @Override
    public Optional<Date> getMinimumAllowedDate() {
        Date result = jdbcTemplate.queryForObject("select s.min_allowed_exchange_date from settings s where s.id = 1", (Map) null, Date.class);
        return  Optional.ofNullable(result);
    }

    @Override
    public Boolean isPeerActive(byte[] peerId) {
        return null;
    }
}
