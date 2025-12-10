package exceptions;

public class FileCorruptedException extends CsvException {

    public FileCorruptedException(String fileName) {
        super("Le fichier CSV '" + fileName + "' est corrompu ou a un format invalide.");
    }
}
