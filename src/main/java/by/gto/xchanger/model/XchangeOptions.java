package by.gto.xchanger.model;


/**
 * Класс для хранения опций импорта
 * и, возможно, в будущем, каких-нибудь еще данных, получаемых по ходу процесса импорта/экспорта
 */
public class XchangeOptions {

    /**
     * только загрузить данные. Все подряд из указанной папки (см. {@link #onlyLoadData_Dir}), без обновления номеров сообщений и формирования ответов
     */
    private boolean onlyLoadData = false;

    /**
     * Загрузить данные из файлов формата belTO, находящихся в папке {@link #onlyLoadData_Dir}
     */
    private boolean importBelTO;

    /**
     * Папка для режима "только загрузить данные" (см. {@link #onlyLoadData})
     */
    private String onlyLoadData_Dir = null;

    /**
     * Версия приложения для помещения в файлы выгрузки
     */
    private String appVersion = "err";

    /**
     * Папка для складывания картинок фотофиксации
     */
    private String photosDirectory = null;

    public String getPhotosDirectory() {
        return photosDirectory;
    }

    public void setPhotosDirectory(String photosDirectory) {
        this.photosDirectory = photosDirectory;
    }

    public boolean isOnlyLoadData() {
        return onlyLoadData;
    }

    public void setOnlyLoadData(boolean onlyLoadData) {
        this.onlyLoadData = onlyLoadData;
    }

    public String getOnlyLoadData_Dir() {
        return onlyLoadData_Dir;
    }

    public void setOnlyLoadData_Dir(String onlyLoadData_Dir) {
        this.onlyLoadData_Dir = onlyLoadData_Dir;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isImportBelTO() {
        return importBelTO;
    }

    public void setImportBelTO(boolean importBelTO) {
        this.importBelTO = importBelTO;
    }
}
