package dao;

import by.gto.library.db.NamedParameterStatement;
import by.gto.library.entity.RegNumberParseResult;
import by.gto.library.helpers.AutoCloseableHelper;
import by.gto.library.helpers.RegNumberHelpers;
import by.gto.library.helpers.TranslitFast;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import model.db.DatabaseTiEntity;
import org.apache.commons.lang3.StringUtils;

public class Dao {
    public final Connection connection;

    public Dao(Connection c) {
        this.connection = c;
    }

    private static final String findEmployeeQuery = "SELECT id FROM ti.employee e WHERE e.name=:name LIMIT 1";
    private static final String insertEmployeeQuery = "INSERT ti.employee (name) VALUES (?)";

    public int findOrInsertEmployee(String empl) throws SQLException {
        String employee = "";
        if (empl != null) {
            employee = empl.trim().toUpperCase();
        }
        try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
            NamedParameterStatement stmtSearch = ach.add(new NamedParameterStatement(connection, findEmployeeQuery));
            stmtSearch.setString("name", employee);
            final ResultSet rs = ach.add(stmtSearch.executeQuery());
            if (rs.next()) {
                return rs.getInt(1);
            }
            PreparedStatement stmtInsert = ach.add(connection.prepareStatement(insertEmployeeQuery, Statement.RETURN_GENERATED_KEYS));
            stmtInsert.setString(1, employee);
            int rows = stmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ошибка создания ti.employee");
            }
            final ResultSet generatedKeys = ach.add(stmtInsert.getGeneratedKeys());
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Ошибка создания ti.employee");
            }
        }
    }

    private static final String findVehicleQuery =
            "SELECT id FROM ti.vehicle v WHERE v.year=:year AND v.chassis_number=:chassis_number AND v.vin_reversed=:vin_reversed LIMIT 1;";
    private static final String insertVehicleQuery = "INSERT INTO ti.vehicle (`year`, chassis_number, vin_reversed) VALUES (?, ?, ?)";

    public int findOrInsertVehicle(short year, String _vin, String _chassis) throws SQLException {
        String vinReversed = "", chassis = "";
        if (_vin != null) {
            vinReversed = StringUtils.reverse(TranslitFast.translitRus2LatWithUppercase(_vin.trim()));
        }
        if (_chassis != null) {
            chassis = TranslitFast.translitRus2LatWithUppercase(_chassis.trim());
        }
        try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
            NamedParameterStatement stmtSearch = ach.add(new NamedParameterStatement(connection, findVehicleQuery));
            stmtSearch.setShort("year", year);
            stmtSearch.setString("chassis_number", chassis);
            stmtSearch.setString("vin_reversed", vinReversed);

            final ResultSet rs = ach.add(stmtSearch.executeQuery());
            if (rs.next()) {
                return rs.getInt(1);
            }
            //`year`, chassis_number, vin_reversed
            PreparedStatement stmtInsert = ach.add(connection.prepareStatement(insertVehicleQuery, Statement.RETURN_GENERATED_KEYS));
            stmtInsert.setShort(1, year);
            stmtInsert.setString(2, chassis);
            stmtInsert.setString(3, vinReversed);
            int rows = stmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ошибка создания ti.vehicle");
            }
            final ResultSet generatedKeys = ach.add(stmtInsert.getGeneratedKeys());
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Ошибка создания ti.vehicle");
            }
        }
    }

    private static final String findPhysicalClientQuery =
            "SELECT id FROM ti.client c WHERE c.name=:name AND c.legal_type=:legal_type AND c.unp IS NULL LIMIT 1;";
    private static final String findJuridicalClientQuery =
            "SELECT id FROM ti.client c WHERE c.name=:name AND c.legal_type=:legal_type AND c.unp = :unp and c.branch is null LIMIT 1;";
    private static final String insertClientQuery =
            "INSERT INTO ti.client (`name`, legal_type, unp) VALUES (?, ?, ?)";

    public int findOrInsertClient(int type, String _name, String _unp) throws SQLException {
        Integer unp;
        try {
            unp = Integer.parseInt(_unp);
        } catch (final NumberFormatException ignored) {
            unp = null;
        }
        String name = StringUtils.substring(StringUtils.trimToEmpty(_name), 0, 300);
        try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
            NamedParameterStatement stmtSearch;
            if (type == 0) {
                stmtSearch = ach.add(new NamedParameterStatement(connection, findPhysicalClientQuery));
                stmtSearch.setString("name", name);
                stmtSearch.setInt("legal_type", type);
            } else {
                stmtSearch = ach.add(new NamedParameterStatement(connection, findJuridicalClientQuery));
                stmtSearch.setString("name", name);
                stmtSearch.setInt("legal_type", type);
                stmtSearch.setInt("unp", unp);
            }
            final ResultSet rs = ach.add(stmtSearch.executeQuery());
            if (rs.next()) {
                return rs.getInt(1);
            }

            PreparedStatement stmtInsert = ach.add(connection.prepareStatement(insertClientQuery, Statement.RETURN_GENERATED_KEYS));
            stmtInsert.setString(1, name);
            stmtInsert.setInt(2, type);
            if (type == 0 || unp == null) {
                stmtInsert.setNull(3, Types.INTEGER);
            } else {
                stmtInsert.setInt(3, unp);
            }
            int rows = stmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ошибка создания ti.client");
            }
            final ResultSet generatedKeys = ach.add(stmtInsert.getGeneratedKeys());
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Ошибка создания ti.client");
            }
        }
    }

    private static final String findRegnumberQuery = "SELECT r.id FROM ti.regnumber r WHERE r.len=:length AND r.letters=:letters AND r.digits=:digits LIMIT 1";
    private static final String insertRegnumberQuery = "INSERT INTO ti.regnumber (letters, digits, len) VALUES (?, ?, ?)";

    public int findOrInsertRegnumber(String regNumber) throws SQLException {
        RegNumberParseResult regNumberParseResult = RegNumberHelpers.parseRegNumber(regNumber);
        if (!regNumberParseResult.isSuccess()) {
            throw new SQLException("Ошибка разбора госномера: " + regNumber);
        }
        regNumberParseResult.setLetters(TranslitFast.translitRus2LatWithUppercase(regNumberParseResult.getLetters()));
        try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
            NamedParameterStatement stmtSearch = ach.add(new NamedParameterStatement(connection, findRegnumberQuery));
            stmtSearch.setInt("length", regNumberParseResult.getLength());
            stmtSearch.setString("letters", regNumberParseResult.getLetters());
            stmtSearch.setInt("digits", regNumberParseResult.getDigits());

            final ResultSet rs = ach.add(stmtSearch.executeQuery());
            if (rs.next()) {
                return rs.getInt(1);
            }
            PreparedStatement stmtInsert = ach.add(connection.prepareStatement(insertRegnumberQuery, Statement.RETURN_GENERATED_KEYS));
            stmtInsert.setString(1, regNumberParseResult.getLetters());
            stmtInsert.setInt(2, regNumberParseResult.getDigits());
            stmtInsert.setInt(3, regNumberParseResult.getLength());
            int rows = stmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ошибка создания ti.regnumber");
            }
            final ResultSet generatedKeys = ach.add(stmtInsert.getGeneratedKeys());
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Ошибка создания ti.regnumber");
            }
        }
    }

    final String insertTiQuery = "INSERT ti.ti (\n"
            + "            employee_id, ds_id, vehicle_id, vehicle_type_id, model_id,\n"
            + "            vehicle_engine_type_id, color_id, card_series, card_number, check_number,\n"
            + "            owner_id, holder_id, reg_cert_series, reg_cert_number, conclusion,\n"
            + "            ti_date, version, checks, reg_number1_id, reg_number2_id,\n"
            + "            guid, possibly_wrong_conclusion, last_modified, dl, dl_digits,\n"
            + "            kilometrage, weight, measurement_method, ecological_class, loaded_by_protocol,\n"
            + "            category_id, reg_number, vin, year, applying,\n"
            + "            total, vat, customer_id, payment_set_id, center_generated_guid,\n"
            + "            flags, source, act_number)\n"
            + "        VALUES (\n"
            + "            :employee_id, :ds_id, :vehicle_id, :vehicle_type_id, :model_id,\n"
            + "            :vehicle_engine_type_id, :color_id, :card_series, :card_number, :check_number,\n"
            + "            :owner_id, :holder_id, :reg_cert_series, :reg_cert_number, :conclusion,\n"
            + "            :ti_date, :version, :checks, :reg_number1_id, :reg_number2_id,\n"
            + "            :guid, :possibly_wrong_conclusion, NOW(), :dl, :dl_digits,\n"
            + "            :kilometrage, :weight, :measurement_method, :ecological_class, :loaded_by_protocol,\n"
            + "            :category_id, :reg_number, :vin, :year, :applying,\n"
            + "            :total, :vat, :customer_id, :payment_set_id, :center_generated_guid,\n" +
            "              :flags, :source, :act_number)\n"
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
            + "            version                   = values(version),\n"
            + "            checks                    = values(checks),\n"
            + "            reg_number1_id            = values(reg_number1_id),\n"
            + "            reg_number2_id            = values(reg_number2_id),\n"
            + "            guid                      = values(guid),\n"
            + "            possibly_wrong_conclusion = values(possibly_wrong_conclusion),\n"
            + "            last_modified             = NOW(),\n"
            + "            dl                        = values(dl),\n"
            + "            dl_digits                 = values(dl_digits),\n"
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
            + "            customer_id               = values(customer_id),\n"
            + "            payment_set_id            = values(payment_set_id),\n"
            + "            center_generated_guid     = values(center_generated_guid),\n"
            + "            flags                     = values(flags),\n"
            + "            source                    = values(source),\n"
            + "            act_number                = values(act_number);";

    public void insertTi(DatabaseTiEntity ti) throws SQLException {
        try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
            NamedParameterStatement stmtInsert = ach.add(new NamedParameterStatement(connection, insertTiQuery));
            stmtInsert.setInt("employee_id", ti.generated.employee_id);
            stmtInsert.setInt("vehicle_id", ti.generated.vehicle_id);
            stmtInsert.setInt("owner_id", ti.generated.owner_id);
            stmtInsert.setInt("holder_id", ti.generated.holder_id);
            stmtInsert.setInt("customer_id", ti.generated.customer_id);
            stmtInsert.setLongNullable("dl_digits", ti.generated.dl_digits);
            stmtInsert.setIntNullable("payment_set_id", ti.generated.payment_set_id);
            stmtInsert.setByte("possibly_wrong_conclusion", ti.generated.possibly_wrong_conclusion);
            stmtInsert.setInt("model_id", ti.generated.model_id);
            stmtInsert.setBytes("checks", ti.generated.checks);
            stmtInsert.setInt("reg_number1_id", ti.generated.reg_number1_id);
            stmtInsert.setIntNullable("reg_number2_id", ti.generated.reg_number2_id);

            stmtInsert.setInt("ds_id", ti.ds_id);
            stmtInsert.setShort("vehicle_type_id", ti.vehicle_type_id);
            stmtInsert.setShort("vehicle_engine_type_id", ti.vehicle_engine_type_id);
            stmtInsert.setInt("color_id", ti.color_id);
            stmtInsert.setString("card_series", ti.card_series);
            stmtInsert.setIntNullable("card_number", ti.card_number);
            stmtInsert.setByte("check_number", ti.check_number);

            stmtInsert.setString("reg_cert_series", ti.reg_cert_series);
            stmtInsert.setInt("reg_cert_number", ti.reg_cert_number);
            stmtInsert.setByte("conclusion", ti.conclusion);
            stmtInsert.setObject("ti_date", ti.ti_date); // ????????????
            stmtInsert.setByte("version", ti.version);
            stmtInsert.setBytes("guid", ti.guid);
            stmtInsert.setIntNullable("kilometrage", ti.kilometrage);
            stmtInsert.setIntNullable("weight", ti.weight);
            stmtInsert.setByteNullable("measurement_method", ti.measurement_method);
            stmtInsert.setByteNullable("ecological_class", ti.ecological_class);
            stmtInsert.setBooleanNullable("loaded_by_protocol", ti.loaded_by_protocol);
            stmtInsert.setByte("category_id", ti.category_id);
            stmtInsert.setString("reg_number", ti.reg_number);
            stmtInsert.setString("vin", ti.vin);
            stmtInsert.setShort("year", ti.year);
            stmtInsert.setByteNullable("applying", ti.applying);
            stmtInsert.setBoolean("center_generated_guid", ti.center_generated_guid);
            stmtInsert.setBigDecimal("total", ti.total);
            stmtInsert.setBigDecimal("vat", ti.vat);
            stmtInsert.setShort("flags", ti.flags);
            stmtInsert.setBytes("source", ti.source);
            stmtInsert.setString("dl", ti.dl);
            stmtInsert.setString("act_number", ti.act_number);
            int rows = stmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ошибка сохранения КДР");
            }
        }
    }
}
