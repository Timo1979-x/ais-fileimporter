package by.gto.xchanger.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageSystem extends AutoCloseable {
    void config(String url, String user, String password);
    List<String> dir(String subdir, String filenamePattern);
    boolean delete(String relativePath);
    InputStream getInputStream(String relativePath);
    OutputStream getOutputStream(String relativePath);
    boolean renameTo(String from, String to);
    void connect();
    void disconnect();
    boolean mkdirs(String relativePath);

    String getUrl();
    void setUrl(String url);
    String getUser();
    void setUser(String user);
    String getPassword();
    void setPassword(String password);
}
