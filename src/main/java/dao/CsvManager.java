package dao;

import model.Note;
import exceptions.CsvException;
import exceptions.FileCorruptedException;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CsvManager {
    private String csvFilePath;
    private Path csvPath;
    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_HEADER = "ID;TITLE;CONTENT;CREATION_DATE;MODIFICATION_DATE;TAGS";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CsvManager(String csvFilePath) throws CsvException {
        this.csvFilePath = csvFilePath;
        this.csvPath = Paths.get(csvFilePath);
        initializeCsvFile();
    }

    private void initializeCsvFile() throws CsvException {
        try {
            if (!Files.exists(csvPath)) {
                Files.createDirectories(csvPath.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }
                System.out.println("Fichier CSV créé: " + csvFilePath);
            }
        } catch (IOException e) {
            throw new CsvException("Impossible de créer le fichier CSV", e);
        }
    }

    public List<Note> readAllNotes() throws CsvException {
        List<Note> notes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (!line.trim().isEmpty()) {
                    try {
                        Note note = parseCsvLine(line);
                        if (note != null) {
                            notes.add(note);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Ligne ignorée (format de date invalide): " + line);
                        continue;
                    }
                }
            }

        } catch (IOException e) {
            throw new CsvException("lecture", csvFilePath, e);
        }

        return notes;
    }


    private Note parseCsvLine(String line) throws CsvException {
        try {
            String[] parts = line.split(CSV_SEPARATOR, -1);

            if (parts.length < 6) {
                throw new FileCorruptedException(csvFilePath,
                        new IllegalArgumentException("Ligne CSV incomplète: " + line));
            }

            Long id = parts[0].isEmpty() ? null : Long.parseLong(parts[0]);
            String title = parts[1];
            String content = parts[2];

            LocalDateTime creationDate = parts[3].isEmpty() ?
                    LocalDateTime.now() :
                    LocalDateTime.parse(parts[3], DATE_FORMATTER);

            LocalDateTime modificationDate = parts[4].isEmpty() ?
                    creationDate :
                    LocalDateTime.parse(parts[4], DATE_FORMATTER);

            Note note = new Note(id, title, content, creationDate, modificationDate);

            if (!parts[5].isEmpty()) {
                note.setTagsFromString(parts[5]);
            }

            return note;

        } catch (NumberFormatException e) {
            throw new FileCorruptedException(csvFilePath, e);
        } catch (DateTimeParseException e) {
            throw new FileCorruptedException(csvFilePath, e);
        }
    }


    public void writeAllNotes(List<Note> notes) throws CsvException {
        try {
            // Créer un fichier temporaire
            Path tempFile = Files.createTempFile("notes", ".csv.tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                // Écrire l'en-tête
                writer.write(CSV_HEADER);
                writer.newLine();

                // Écrire chaque note
                for (Note note : notes) {
                    String csvLine = convertNoteToCsvLine(note);
                    writer.write(csvLine);
                    writer.newLine();
                }
            }

            Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new CsvException("écriture", csvFilePath, e);
        }
    }


    private String convertNoteToCsvLine(Note note) {
        String[] parts = new String[6];

        parts[0] = note.getId() != null ? note.getId().toString() : "";
        parts[1] = escapeCsvField(note.getTitle());
        parts[2] = escapeCsvField(note.getContent());
        parts[3] = note.getFormattedCreationDate();
        parts[4] = note.getFormattedModificationDate();
        parts[5] = escapeCsvField(note.getTagsAsString());

        return String.join(CSV_SEPARATOR, parts);
    }


    private String escapeCsvField(String field) {
        if (field == null) return "";

        if (field.contains(CSV_SEPARATOR) || field.contains("\"") || field.contains("\n")) {
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }


    public void addNote(Note note) throws CsvException {
        try {
            List<Note> notes = readAllNotes();

            if (note.getId() == null) {
                Long maxId = notes.stream()
                        .map(Note::getId)
                        .filter(Objects::nonNull)
                        .max(Long::compare)
                        .orElse(0L);
                note.setId(maxId + 1);
            }

            notes.add(note);

            writeAllNotes(notes);

        } catch (CsvException e) {
            throw new CsvException("Impossible d'ajouter la note", e);
        }
    }

    public void updateNote(Note updatedNote) throws CsvException {
        try {
            List<Note> notes = readAllNotes();
            boolean found = false;

            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                if (note.getId() != null && note.getId().equals(updatedNote.getId())) {
                    notes.set(i, updatedNote);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new CsvException("Note non trouvée pour mise à jour: ID=" + updatedNote.getId());
            }

            writeAllNotes(notes);

        } catch (CsvException e) {
            throw new CsvException("Impossible de mettre à jour la note", e);
        }
    }


    public void deleteNote(Long noteId) throws CsvException {
        try {
            List<Note> notes = readAllNotes();

            List<Note> filteredNotes = notes.stream()
                    .filter(note -> !note.getId().equals(noteId))
                    .toList();

            if (filteredNotes.size() == notes.size()) {
                throw new CsvException("Note non trouvée pour suppression: ID=" + noteId);
            }

            writeAllNotes(filteredNotes);

        } catch (CsvException e) {
            throw new CsvException("Impossible de supprimer la note", e);
        }
    }


    public synchronized void autoSave(List<Note> notes) throws CsvException {
        try {
            writeAllNotes(notes);
            System.out.println("Sauvegarde automatique effectuée à " + LocalDateTime.now());
        } catch (CsvException e) {
            System.err.println("Échec de la sauvegarde automatique: " + e.getMessage());
            throw e;
        }
    }


    public void createBackup() throws CsvException {
        try {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = csvFilePath.replace(".csv", "backup" + timestamp + ".csv");
            Path backupPath = Paths.get(backupFileName);

            Files.copy(csvPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Sauvegarde créée: " + backupFileName);

        } catch (IOException e) {
            throw new CsvException("Impossible de créer la sauvegarde", e);
        }
    }


    public List<Note> importFromCsv(String sourceFilePath) throws CsvException {
        try {
            Path sourcePath = Paths.get(sourceFilePath);
            if (!Files.exists(sourcePath)) {
                throw new CsvException("Fichier source introuvable: " + sourceFilePath);
            }

            List<Note> importedNotes = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(sourcePath)) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    if (!line.trim().isEmpty()) {
                        try {
                            Note note = parseCsvLine(line);
                            if (note != null) {
                                importedNotes.add(note);
                            }
                        } catch (DateTimeParseException e) {
                            System.err.println("Ligne ignorée lors de l'import: " + line);
                            continue;
                        }
                    }
                }
            }

            return importedNotes;

        } catch (IOException e) {
            throw new CsvException("Impossible d'importer depuis le fichier", e);
        }
    }


    public void exportToCsv(List<Note> notes, String destinationFilePath) throws CsvException {
        try {
            Path destPath = Paths.get(destinationFilePath);
            Files.createDirectories(destPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(destPath)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (Note note : notes) {
                    String csvLine = convertNoteToCsvLine(note);
                    writer.write(csvLine);
                    writer.newLine();
                }
            }

            System.out.println("Export terminé: " + destinationFilePath);

        } catch (IOException e) {
            throw new CsvException("Impossible d'exporter vers le fichier", e);
        }
    }


    public boolean validateCsvFormat() throws CsvException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String header = reader.readLine();
            if (header == null || !header.equals(CSV_HEADER)) {
                return false;
            }

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 10) {
                if (!line.trim().isEmpty()) {
                    parseCsvLine(line); // Lève une exception si la ligne est invalide
                }
                lineCount++;
            }

            return true;

        } catch (IOException e) {
            throw new CsvException("Impossible de valider le fichier CSV", e);
        }
    }

    public String getCsvFilePath() { return csvFilePath; }

    public boolean fileExists() {
        return Files.exists(csvPath);
    }

    public long getFileSize() throws IOException {
        return Files.size(csvPath);
    }

    //streams

    public List<Note> findByTag(String tag) throws CsvException {
        return readAllNotes().stream()
                .filter(note -> note.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public List<Note> findByDate(LocalDateTime date) throws CsvException {
        return readAllNotes().stream()
                .filter(note -> note.getDateCreation().isEqual(date))
                .collect(Collectors.toList());
    }

    public List<Note> findByKeyword(String keyword) throws CsvException {
        return readAllNotes().stream()
                .filter(note -> note.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || note.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Note> sortByDate() throws CsvException {
        return readAllNotes().stream()
                .sorted(Comparator.comparing(Note::getDateCreation))
                .collect(Collectors.toList());
    }

    public List<Note> sortByTitle() throws CsvException {
        return readAllNotes().stream()
                .sorted(Comparator.comparing(Note::getTitle))
                .collect(Collectors.toList());
    }


}