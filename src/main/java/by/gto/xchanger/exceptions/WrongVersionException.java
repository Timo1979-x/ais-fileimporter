package by.gto.xchanger.exceptions;

public class WrongVersionException extends RootXchangeException {
    public WrongVersionException() {
    }

    public WrongVersionException(String message) {
        super(message);
    }
}
