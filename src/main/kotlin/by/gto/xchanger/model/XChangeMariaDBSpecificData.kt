package by.gto.xchanger.model

class XChangeMariaDBSpecificData: XChangeDBSpecificData() {
    init {
        entities = arrayListOf(
                EntityDescriptor(
                        "id", "DS", "ds_info_current",
                        mapOf("shortname" to "shortname", "number" to "ds_number")
                ),
                EntityDescriptor(
                        "id", "V_Model", "model",
                        mapOf("name_rus" to "name", "name_eng" to "eng_name")
                ),
                EntityDescriptor(
                        "id", "V_Category", "category",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                ),
                EntityDescriptor(
                        "id", "V_Color", "color",
                        mapOf("name" to "name")
                ),
                EntityDescriptor(
                        "id", "V_Type", "vehicle_type",
                        mapOf("name" to "name", "common_name" to "common_name")
                ),
                EntityDescriptor(
                        "id", "V_EngType", "vehicle_engine_type",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                ),
                EntityDescriptor(
                        "id", "V_Applying", "applying",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                )
        )
    }
}