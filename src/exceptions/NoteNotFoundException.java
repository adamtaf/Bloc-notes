package exceptions;

public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(String msg) { super(msg); }
}