package by.gto.xchanger.model;

import by.gto.xchanger.storage.StorageSystem;

import java.util.Date;
import java.util.Map;

public class XChangeParameters {
    private String appVersion = "err";
    private String dbVersion = "err";
    StorageSystem inputStorageSystem;
    StorageSystem outputStorageSystem;
    XChangeCachedData cachedData;
    byte[] myID;
    Date exportDate;

    public XChangeCachedData getCachedData() {
        return cachedData;
    }

    public void setCachedData(XChangeCachedData cachedData) {
        this.cachedData = cachedData;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }


    public StorageSystem getInputStorageSystem() {
        return inputStorageSystem;
    }

    public void setInputStorageSystem(StorageSystem inputStorageSystem) {
        this.inputStorageSystem = inputStorageSystem;
    }

    public StorageSystem getOutputStorageSystem() {
        return outputStorageSystem;
    }

    public void setOutputStorageSystem(StorageSystem outputStorageSystem) {
        this.outputStorageSystem = outputStorageSystem;
    }

    public byte[] getMyID() {
        return myID;
    }

    public void setMyID(byte[] myID) {
        this.myID = myID;
    }

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }
}
