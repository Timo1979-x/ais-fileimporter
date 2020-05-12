package by.gto.xchanger.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Хранит результаты импорта по протоколу: Набор импортированных госномеров и техпаспортов
 */
public class XChangeResults {
    private Set<String> regNumberSet = new HashSet<>();
    private Set<String> certNumberSet = new HashSet<>();
    private byte[] importedSenderGUID;
    private Throwable error;

    public Set<String> getRegNumberSet() {
        return regNumberSet;
    }

    public void setRegNumberSet(Set<String> regNumberSet) {
        this.regNumberSet = regNumberSet;
    }

    public Set<String> getCertNumberSet() {
        return certNumberSet;
    }

    public void setCertNumberSet(Set<String> certNumberSet) {
        this.certNumberSet = certNumberSet;
    }

    public byte[] getImportedSenderGUID() {
        return importedSenderGUID;
    }

    public void setImportedSenderGUID(byte[] importedSenderGUID) {
        this.importedSenderGUID = importedSenderGUID;
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
