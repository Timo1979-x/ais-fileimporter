package by.gto.xchanger.model;

import java.util.List;

public abstract class XChangeDBSpecificData {
    protected static List<EntityDescriptor> entities;

    public static List<EntityDescriptor> getEntities() {
        return entities;
    }

    public void finish() {
    }
}
