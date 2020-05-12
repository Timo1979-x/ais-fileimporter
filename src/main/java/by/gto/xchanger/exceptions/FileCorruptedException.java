package by.gto.xchanger.exceptions;

public class FileCorruptedException extends RootXchangeException {
    public FileCorruptedException() {
    }

    public FileCorruptedException(String message) {
        super(message);
    }
}
