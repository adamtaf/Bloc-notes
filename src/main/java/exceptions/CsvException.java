package exceptions;

import java.io.IOException;

public class CsvException extends Exception {

    public CsvException(String message) {
        super(message);
    }

    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvException(String lecture, String csvFilePath, IOException e) {
        super(csvFilePath, e);
    }
}