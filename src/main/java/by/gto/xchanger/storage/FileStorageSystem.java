package by.gto.xchanger.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileStorageSystem implements StorageSystem {
    private String url;
    private String user;
    private String password;

    public FileStorageSystem() {
    }

    public FileStorageSystem(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void config(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public List<String> dir(String subdir, final String filenamePattern) {
        File dir = new File(url + File.separator + subdir);
        return Arrays.asList(
                dir.list((dir1, name) -> name.matches(filenamePattern))
        );
    }

    @Override
    public boolean delete(String relativePath) {
        return new File(url, relativePath).delete();
    }

    @Override
    public OutputStream getOutputStream(String relativePath) {
        File f = new File(url, relativePath);
        try {
            return new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public InputStream getInputStream(String relativePath) {
        File f = new File(url, relativePath);
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean renameTo(String from, String to) {
        mkdirs(Paths.get(to).getParent().toString());
        Path srcFile = Paths.get(url, from);
        try {
            Files.move(srcFile, Paths.get(url, to));
            return true;
        } catch (FileAlreadyExistsException e) {

            try {
                Files.delete(srcFile);
            } catch (IOException ignored) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
//        return new File(url, from).renameTo(
//                new File(url, to)
//        );
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean mkdirs(String relativePath) {
        return new File(url, relativePath).mkdirs();
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void close() throws Exception {
    }
}
