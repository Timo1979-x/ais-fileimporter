package by.gto.xchanger.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class XChangeCachedData {
    private Map<Integer, Integer> mapFirmCodeToId = new HashMap<>();
    private int[] checksMap = new int[0];
    private Map<Integer, Integer> mapOurModelIdToGaiModelId = new HashMap<>();
    private LocalDate minimumAllowedDate;

    public XChangeCachedData() {
    }

    public XChangeCachedData(Map<Integer, Integer> mapFirmCodeToId, int[] checksMap) {
        this.mapFirmCodeToId = mapFirmCodeToId;
        this.checksMap = checksMap;
    }

    public XChangeCachedData(Map<Integer, Integer> mapFirmCodeToId, int[] checksMap, Map<Integer, Integer> mapOurModelIdToGaiModelId) {
        this.mapFirmCodeToId = mapFirmCodeToId;
        this.checksMap = checksMap;
        this.mapOurModelIdToGaiModelId = mapOurModelIdToGaiModelId;
    }

    public Map<Integer, Integer> getMapFirmCodeToId() {
        return mapFirmCodeToId;
    }

    public void setMapFirmCodeToId(Map<Integer, Integer> mapFirmCodeToId) {
        this.mapFirmCodeToId = mapFirmCodeToId;
    }

    public int[] getChecksMap() {
        return checksMap;
    }

    public void setChecksMap(int[] checksMap) {
        this.checksMap = checksMap;
    }

    public Map<Integer, Integer> getMapOurModelIdToGaiModelId() {
        return mapOurModelIdToGaiModelId;
    }

    public void setMapOurModelIdToGaiModelId(Map<Integer, Integer> mapOurModelIdToGaiModelId) {
        this.mapOurModelIdToGaiModelId = mapOurModelIdToGaiModelId;
    }

    public LocalDate getMinimumAllowedDate() {
        return minimumAllowedDate;
    }

    public void setMinimumAllowedDate(LocalDate minimumAllowedDate) {
        this.minimumAllowedDate = minimumAllowedDate;
    }
}
