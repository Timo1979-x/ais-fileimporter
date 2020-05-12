package by.gto.xchanger.exceptions;

public class ProcessingErrorException  extends RootXchangeException {
    public ProcessingErrorException() {
    }

    public ProcessingErrorException(String message) {
        super(message);
    }
}
