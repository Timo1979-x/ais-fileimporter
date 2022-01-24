package model.db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseTiEntity {
    /**
     * Содержит поля, которые перед записью необходимо сформировать каким-либо образом.
     */
    public static class Generated {
        public int employee_id;
        public int vehicle_id;
        public int owner_id;
        public int holder_id;
        public int customer_id;
        @Nullable
        public Long dl_digits;
        @Nullable
        public Integer payment_set_id = null;
        public byte possibly_wrong_conclusion = 0;
        public int model_id;
        @NotNull
        public byte[] checks;
        public int reg_number1_id;
        @Nullable
        public Integer reg_number2_id = null;
    }

    public Generated generated = new Generated();

    public int ds_id;
    public short vehicle_type_id;
    public short vehicle_engine_type_id;
    public int color_id;
    @Nullable
    public String card_series;
    @Nullable
    public Integer card_number;
    public byte check_number;
    @NotNull
    public String reg_cert_series = "";
    public int reg_cert_number = 0;
    public byte conclusion;
    public LocalDateTime ti_date;
    public byte version;
    public byte[] guid;
    @Nullable
    public Integer kilometrage;
    public Integer weight;
    @Nullable
    public Byte measurement_method;
    @Nullable
    public Byte ecological_class;
    public boolean loaded_by_protocol = true;
    public byte category_id;
    @NotNull
    public String reg_number;
    @Nullable
    public String vin;
    public short year;
    @Nullable
    public Byte applying;
    public boolean center_generated_guid = false;
    @Nullable
    public BigDecimal total = null;
    @Nullable
    public BigDecimal vat = null;
    public short flags = 0;
    public byte[] source = null;
    public String dl = null;

    @Nullable
    public String act_number = null;
}
