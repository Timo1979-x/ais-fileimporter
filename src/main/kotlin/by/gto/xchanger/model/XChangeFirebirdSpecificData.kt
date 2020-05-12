package by.gto.xchanger.model

class XChangeFirebirdSpecificData : XChangeDBSpecificData() {
    init {
        entities = arrayListOf(
                EntityDescriptor(
                        "code", "DS", "DS",
                        mapOf("shortname" to "shortname", "ds_number" to "ds_number")
                ),
                EntityDescriptor(
                        "id", "V_Model", "MODELSTC",
                        mapOf("name" to "name", "eng_name" to "eng_name")
                ),
                EntityDescriptor(
                        "id", "V_Category", "REF_CATEGORIES",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                ),
                EntityDescriptor(
                        "id", "V_Color", "COLORSTC",
                        mapOf("name" to "name")
                ),
                EntityDescriptor(
                        "id", "V_Type", "TYPETC",
                        mapOf("name" to "name", "common_name" to "common_name")
                ),
                EntityDescriptor(
                        "id", "V_EngType", "TYPEENGINESTC",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                ),
                EntityDescriptor(
                        "id", "V_Applying", "applyings",
                        mapOf("name" to "name", "bit_value" to "bit_value")
                )
        );
    }
}