package by.gto.xchanger.dao.jdbc;

import by.gto.erip.model.LegalTypesEnum;
import by.gto.erip.model2.Client;
import by.gto.library.entity.RegNumberParseResult;
import by.gto.xchanger.dao.XChangerDao;
import by.gto.xchanger.helpers.Commons;
import by.gto.xchanger.model.EntityDescriptor;
import by.gto.xml.entities.DiagCard;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

public class XChangerDaoMariaDB implements XChangerDao {

    private final List<Client> clientsWithId = new ArrayList<>(6);

    {
        // emptyClient - физлицо с пустым именем. Всегда имеет id=0 в БД
        // Добавляем в список чтобы каждый раз такого не создавать в БД
        Client emptyClient = new Client("", LegalTypesEnum.INDIVIDUAL, null);
        emptyClient.setId(0);
        clientsWithId.add(emptyClient);
    }

    protected NamedParameterJdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Map<Integer, Integer> getCodesToIdsMaping() {
        return null;
    }

    @Override
    public int[] getChecksMapping() {
        return null;
    }

    @Override
    public byte[] getMyGuid() {
        final List<byte[]> result = new ArrayList<>();
        jdbcTemplate.query("SELECT id FROM ti.peers WHERE me != 0 LIMIT 1", resultSet -> {
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
    public Integer getReceivedMessageNumber(byte[] peerId) {
        final List<Integer> r = new ArrayList<>();
        String sqlQuery = "SELECT\n"
            + "  IFNULL(peers.last_received_message, -1),\n"
            + "  CAST(peer_active AS int)\n"
            + "FROM ti.peers\n"
            + "WHERE peers.id = :peerId";
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
    public int createPeer(byte[] peerId, int msgFormatVersion) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("format_version", msgFormatVersion);
        return jdbcTemplate.update("INSERT INTO ti.peers (id, format_version) VALUES (:peer_id, :format_version)", params);
    }

    @Override
    public int deletePti(byte[] toDel, LocalDate minimumAllowedDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("guid", toDel);
        params.put("minimumAllowedDate", minimumAllowedDate);
        return jdbcTemplate.update("DELETE FROM ti.ti WHERE guid = :guid and ti.ti.ti_date >= :minimumAllowedDate", params);
    }

    @Override
    public int insertOrUpdatePTI(DiagCard dc, boolean loadedByProtocol) {
        final String sql = "INSERT ti.ti (\n"
            + "            employee_id, ds_id, vehicle_id, vehicle_type_id, model_id,\n"
            + "            vehicle_engine_type_id, color_id, card_series, card_number, check_number,\n"
            + "            owner_id, holder_id, reg_cert_series, reg_cert_number, conclusion,\n"
            + "            ti_date, version, checks, reg_number1_id, reg_number2_id,\n"
            + "            guid, possibly_wrong_conclusion, last_modified, dl_series, dl_number,\n"
            + "            kilometrage, weight, measurement_method, ecological_class, loaded_by_protocol,\n"
            + "            category_id, reg_number, vin, year, applying, total, vat, customer_id)\n"
            + "        VALUES (\n"
            + "            :employee_id, :firm_code, :vehicle_id, :IDTYPETC, :gai_model_id,\n"
            + "            :IDTYPEENGINESTC, :IDCOLORSTC, :SERIAL, :SERIALNUMBER, :SECONDCHECK + 1,\n"
            + "            :owner_id, :holder_id, :SPSERIAL, :SPNUMBER, :CONDITIONTC,\n"
            + "            :DATETO, 3, :checks_packed, :reg_number1_id, 0,\n"
            + "            :guid_bytes, :POSSIBLY_WRONG_CONDITION, NOW(), :DL_SERIES, :DL_NUMBER,\n"
            + "            :KILOMETRAGE, :WEIGHT, :MEASUREMENT_METHOD, :ECOLOGICAL_CLASS, 1,\n"
            + "            :CATEGORY_ID, :normalized_rn, :VIN, :DATEAUTO, :APPLYING, :total, :vat, :customer_id)\n"
            + "        ON DUPLICATE KEY UPDATE\n"
            + "            employee_id               = values(employee_id),\n"
            + "            ds_id                     = values(ds_id),\n"
            + "            vehicle_id                = values(vehicle_id),\n"
            + "            vehicle_type_id           = values(vehicle_type_id),\n"
            + "            model_id                  = values(model_id),\n"
            + "            vehicle_engine_type_id    = values(vehicle_engine_type_id),\n"
            + "            color_id                  = values(color_id),\n"
            + "            card_series               = values(card_series),\n"
            + "            card_number               = values(card_number),\n"
            + "            check_number              = values(check_number),\n"
            + "            owner_id                  = values(owner_id),\n"
            + "            holder_id                 = values(holder_id),\n"
            + "            reg_cert_series           = values(reg_cert_series),\n"
            + "            reg_cert_number           = values(reg_cert_number),\n"
            + "            conclusion                = values(conclusion),\n"
            + "            ti_date                   = values(ti_date),\n"
            + "            version                   = values(version),\n"
            + "            checks                    = values(checks),\n"
            + "            reg_number1_id            = values(reg_number1_id),\n"
            + "            reg_number2_id            = values(reg_number1_id),\n"
            + "            guid                      = values(guid),\n"
            + "            possibly_wrong_conclusion = values(possibly_wrong_conclusion),\n"
            + "            last_modified             = NOW(),\n"
            + "            dl_series                 = values(dl_series),\n"
            + "            dl_number                 = values(dl_number),\n"
            + "            kilometrage               = values(kilometrage),\n"
            + "            weight                    = values(weight),\n"
            + "            measurement_method        = values(measurement_method),\n"
            + "            ecological_class          = values(ecological_class),\n"
            + "            loaded_by_protocol        = values(loaded_by_protocol),\n"
            + "            category_id               = values(category_id),\n"
            + "            reg_number                = values(reg_number),\n"
            + "            vin                       = values(vin),\n"
            + "            year                      = values(year),\n"
            + "            applying                  = values(applying),\n"
            + "            total                     = values(total),\n"
            + "            vat                       = values(vat),\n"
            + "            customer_id               = values(customer_id)";


        int employeeId = findOrInsertEmployee(dc.getEMPLOYEE());
        int vehicleId = findOrInsertVehicle(dc);

        dealwithClients(dc);

        RegNumberParseResult regNumberParseResult = dc.getRegNumberParseResult();
        int regNumberId = findOrInsertRegNumber(regNumberParseResult.getLetters(),
            regNumberParseResult.getDigits(), regNumberParseResult.getLength());

        dc.setEmployeeId(employeeId);
        dc.setVehicleId(vehicleId);
        dc.setRegNumberId(regNumberId);

        Map<String, Object> paramMap = Commons.fillDiagCardParams(dc, loadedByProtocol);
        // LocalTime time = LocalDateTime.ofInstant(dc.getDATETO().toInstant(), ZoneId.systemDefault()).toLocalTime();
        return jdbcTemplate.update(sql, paramMap);
    }

    private void dealwithClients(DiagCard dc) {
        List<ImmutablePair<Consumer<Integer>, Client>> l = new ArrayList<>(3);
        l.add(new ImmutablePair<>(dc::setOwnerId, new Client(dc.getOWNER(), (byte) dc.getOwnerType(), dc.getOWNERUNP())));
        l.add(new ImmutablePair<>(dc::setCustomerId, new Client(dc.getCustomer(), (byte) dc.getCustomerType(), dc.getCustomerUnp())));
        l.add(new ImmutablePair<>(dc::setHolderId, new Client(dc.getPARENT(), LegalTypesEnum.INDIVIDUAL, null)));

        for (ImmutablePair<Consumer<Integer>, Client> pair : l) {
            Client c = pair.right;
            c.preSaveActionsAndChecks();
            int i = clientsWithId.indexOf(c);
            if (i == -1) {
                findOrInsertClient(c);
                clientsWithId.add(c);
                pair.left.accept(c.getId());
            } else {
                pair.left.accept(clientsWithId.get(i).getId());
            }
        }
        if (dc.getCustomerId() == 0) {
            dc.setCustomerId(dc.getHolderId());
        }
    }

    private int findOrInsertRegNumber(String letters, int digits, int length) {
        String sqlQuery = "SELECT r.id FROM ti.regnumber r WHERE r.len=:length AND r.letters=:letters AND r.digits=:digits LIMIT 1;";
        MapSqlParameterSource params = new MapSqlParameterSource("letters", letters);
        params.addValue("digits", digits);
        params.addValue("length", length);
        int[] ids = {-1};
        jdbcTemplate.query(sqlQuery, params,
            resultSet -> {
                ids[0] = resultSet.getInt(1);
            });
        if (ids[0] != -1) {
            return ids[0];
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            "INSERT INTO ti.regnumber (letters, digits, len) VALUES (:letters, :digits, :length)",
            params, keyHolder);
        return keyHolder.getKey().intValue();
    }

    private void findOrInsertClient(Client c) {
        String sqlQuery;
        if (c.getUnp() == null || c.getLegalType() == 0) {
            sqlQuery = "SELECT id FROM ti.client c WHERE c.name=:name AND c.legal_type=:legal_type AND c.unp IS NULL LIMIT 1;";
            c.setUnp(null);
        } else {
            sqlQuery = "SELECT id FROM ti.client c WHERE c.name=:name AND c.legal_type=:legal_type AND c.unp = :unp LIMIT 1;";
        }
        MapSqlParameterSource params = new MapSqlParameterSource("name", c.getName());
        params.addValue("legal_type", c.getLegalType());
        params.addValue("unp", c.getUnp());

        int[] ids = {-1};
        jdbcTemplate.query(sqlQuery, params,
            resultSet -> {
                ids[0] = resultSet.getInt(1);
            });
        if (ids[0] != -1) {
            c.setId(ids[0]);
            return;
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("INSERT INTO ti.client (`name`, legal_type, unp) VALUES (:name, :legal_type, :unp)", params, keyHolder);
        c.setId(keyHolder.getKey().intValue());
    }

    private int findOrInsertVehicle(DiagCard dc) {
        String sqlQuery =
            "SELECT id FROM ti.vehicle v WHERE v.year=:year AND v.chassis_number=:chassis_number AND v.vin_reversed=:vin_reversed LIMIT 1;";
        MapSqlParameterSource params = new MapSqlParameterSource("year", dc.getDATEAUTO());
        params.addValue("chassis_number", StringUtils.isEmpty(dc.getSHNUMBER()) ? "" : StringUtils.substring(dc.getSHNUMBER(), 0, 40));
        params.addValue("vin_reversed", StringUtils.substring(dc.getREVERSEVIN(), 0, 18));
        int[] ids = {-1};
        jdbcTemplate.query(sqlQuery, params,
            resultSet -> {
                ids[0] = resultSet.getInt(1);
            });
        if (ids[0] != -1) {
            return ids[0];
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            "INSERT INTO ti.vehicle (`year`, chassis_number, vin_reversed) VALUES (:year, :chassis_number, :vin_reversed)", params, keyHolder);
        return keyHolder.getKey().intValue();
    }

    private int findOrInsertEmployee(String employee) {
        String sqlQuery = "SELECT id FROM ti.employee e WHERE e.name=:name LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource("name", employee);
        int[] ids = {-1};
        jdbcTemplate.query(sqlQuery, params,
            resultSet -> {
                ids[0] = resultSet.getInt(1);
            });
        if (ids[0] != -1) {
            return ids[0];
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("INSERT ti.employee (name) VALUES (:name)", params, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int updateRegistry(byte[] peerId, int successfullyReceivedMessageNumber) {
        String sql = "DELETE FROM ti.changes_registry WHERE peer_id = :peer_id AND message_number <= :message_number";
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("message_number", successfullyReceivedMessageNumber);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int updateMessageNumberInPeerTable(byte[] peerId, int msgNumber, Long dsCode, int msgFormatVersion) {
        final String sql = "UPDATE ti.peers\n"
            + "SET peers.peer_active = 1,\n"
            + "    peers.format_version = :msg_format_version,\n"
            + "    peers.last_received_message = :last_received_message,\n"
            + "    peers.last_received_time = NOW()\n"
            + ((dsCode != null) ? "    ,peers.ds_id = :ds_code\n" : "")
            + "WHERE peers.id = :peer_id";
        Map<String, Object> params = new HashMap<>();
        params.put("last_received_message", msgNumber);

        if (dsCode != null) {
            params.put("ds_code", dsCode.intValue());
        }
        params.put("peer_id", peerId);
        params.put("msg_format_version", msgFormatVersion);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public List<byte[]> findAdditionalPeers(Date date) {
        final List<byte[]> r = new ArrayList<>();
        String sql = "SELECT DISTINCT\n"
            + "        changes_registry.peer_id AS idd\n"
            + "        FROM ti.changes_registry\n"
            + "        LEFT JOIN ti.peers\n"
            + "        ON (peers.id = changes_registry.peer_id)\n"
            + "        WHERE changes_registry.message_number IS NULL\n"
            + "        AND changes_registry.start_date <= :date\n"
            + "        AND peers.peer_active != 0\n"
            + "        AND peers.me = 0\n"
            + "        UNION\n"
            + "        SELECT\n"
            + "        peers.id\n"
            + "        FROM ti.peers\n"
            + "        LEFT JOIN ti.changes_registry\n"
            + "        ON (peers.id = changes_registry.peer_id)\n"
            + "        WHERE peers.peer_active != 0\n"
            + "        AND ((peers.last_sent_time IS NULL)\n"
            + "        OR DATE_ADD(peers.last_sent_time, INTERVAL 1 DAY) < NOW()\n"
            + "        )\n"
            + "        GROUP BY peers.id\n"
            + "        HAVING COUNT(changes_registry.peer_id) > 0";
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        jdbcTemplate.query(sql, params, resultSet -> {
            r.add(resultSet.getBytes(1));
        });
        return r;
    }

    @Override
    public List<Integer> getMessageNumbers(byte[] peerId) {
        final List<Integer> r = new ArrayList<>();
        String sqlQuery = "SELECT COALESCE(p.last_sent_message, 0), COALESCE(p.LAST_RECEIVED_MESSAGE, 0), p.FORMAT_VERSION FROM ti.peers p\n"
            + "WHERE id = :peer_id";
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
        params.put("peer_id", peerId);
        params.put("last_sent_message", lastSentMessage);
        jdbcTemplate.update(
            "UPDATE ti.peers SET last_sent_message = :last_sent_message, last_sent_time = NOW() WHERE id = :peer_id",
            params);
    }

    @Override
    public List<Map<String, Object>> getChanges(EntityDescriptor entityDescriptor, byte[] peerId, Date exportDate) {
        return Commons.getChanges(entityDescriptor, peerId, jdbcTemplate, exportDate);
    }

    @Override
    public List<Integer> getDeletions(String entityName, byte[] peerId, Date exportDate) {
        return Commons.getDeletions(entityName, peerId, jdbcTemplate, exportDate);
    }

    //    @Override
    //    public void exportEntity(byte[] peerId, EntityDescriptor entityDescriptor, List<Integer> deletions, List<Map<String, Object>> changes) {
    //        1 if (deletions == null || changes == null) {
    //            throw new ProcessingErrorException("deletions == null || changes == null");
    //        }
    //        deletions.clear();
    //        changes.clear();
    //
    //        String idFieldName = entityDescriptor.getIdFieldName();
    //        deletions.addAll(xChangeMappers.getDeletions(peerId, entityDescriptor.getEntityName()));
    //        List<Map<String, Object>> _changes = xChangeMappers.getChanges(
    //                peerId, entityDescriptor.getTableName(), idFieldName, entityDescriptor.getEntityName(),
    //                Arrays.stream(entityDescriptor.getFieldsToExport()).map(e -> e[0]).collect(Collectors.toList())
    //        );
    //        changes.addAll(_changes);
    //    }

    @Override
    public void updateChangesRegistry(byte[] senderId, int msgNumber, Date exportDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", senderId);
        params.put("message_number", msgNumber);
        params.put("export_date", exportDate);
        jdbcTemplate.update(
            "UPDATE ti.changes_registry "
                + "SET message_number = :message_number "
                + "WHERE peer_id = :peer_id AND message_number IS NULL and start_date <= :export_date",
            params);
    }

    @Override
    public void registerReferenceForPeer(byte[] peerId, EntityDescriptor entityDescriptor) {
        Map<String, Object> params = new HashMap<>();
        params.put("peer_id", peerId);
        params.put("entity_name", entityDescriptor.getEntityName());

        jdbcTemplate.update("DELETE FROM ti.changes_registry WHERE `name` = :entity_name AND peer_id = :peer_id", params);
        String query = String.format("INSERT INTO changes_registry (peer_id, name, entity_id_int32, deletion)\n"
                + "  SELECT :peer_id, :entity_name, %1$s.%2$s, 0 from %1$s where !%1$s.not_for_export",
            entityDescriptor.getTableName(), entityDescriptor.getIdFieldName());
        jdbcTemplate.update(query, params);


        if ("DS".equals(entityDescriptor.getEntityName())) {
            String q = "INSERT INTO ti.changes_registry (peer_id, `name`, entity_id_int32, deletion, start_date)\n"
                + "      SELECT :peer_id, :entity_name, ds.id, 0, dsi.date1\n"
                + "        from ti.ds ds\n"
                + "        INNER JOIN ti.ds_info dsi ON (dsi.ds_id=ds.id AND dsi.date1 > now())\n"
                + "      WHERE !ds.not_for_export;";
            jdbcTemplate.update(q, params);
        }
    }

    @Override
    public String getDBVersion() {
        final String[] result = new String[]{"_err_"};
        jdbcTemplate.query("SELECT CONCAT(features,'.', structure) FROM ti.versions LIMIT 1", (Map<String, ?>) null, resultSet -> {
            result[0] = "" + resultSet.getString(1);
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
        Map<String, Object> params = new HashMap<>();
        params.put("days", days);
        return jdbcTemplate.update("UPDATE ti.peers SET peer_active = 0 "
            + "WHERE peer_active = 1 AND last_received_time < DATE_ADD(now(), INTERVAL - :days DAY) AND me != 1", params);
    }

    @Override
    public Map<Integer, Integer> getMapOurModelIdToGaiModelId() {
        Map<Integer, Integer> r = new HashMap<>(10000);
        jdbcTemplate.query("SELECT id, gai_id FROM ti.model", rs -> {
            r.put(rs.getInt(1), rs.getInt(2));
        });
        return r;
    }

    @Override
    public Optional<Date> getMinimumAllowedDate() {
        final Date[] result = new Date[]{null};

        jdbcTemplate.query(
            "SELECT s.min_allowed_exchange_date FROM ti.settings s WHERE s.application IN "
                + "('*default*', 'FileImporter.jar') ORDER BY s.application;", rs -> {
                final java.sql.Date date = rs.getDate(1);
                if (date != null) {
                    result[0] = date;
                }
            });

        return result[0] == null ? Optional.empty() : Optional.of(new Date(result[0].getTime()));
    }
}
