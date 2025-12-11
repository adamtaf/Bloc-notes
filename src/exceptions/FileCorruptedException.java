package exceptions;


public class FileCorruptedException extends CsvException {

    public FileCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}