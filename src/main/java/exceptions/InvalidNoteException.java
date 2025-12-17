package exceptions;

public class InvalidNoteException extends RuntimeException {
    public InvalidNoteException(String msg) { super(msg); }
}
