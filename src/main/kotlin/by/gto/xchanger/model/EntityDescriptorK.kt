package by.gto.xchanger.model

data class EntityDescriptor(val idFieldName: String, val entityName: String, val tableName: String, val fieldsToExport: Map<String, String>)
