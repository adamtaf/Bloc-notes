package dao;

import model.Note;
import utils.CSVUtils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DAO CSV compatible avec le CsvManager de la collègue :
 * Header : ID;TITLE;CONTENT;CREATION_DATE;MODIFICATION_DATE;TAGS
 * Séparateur : ';'
 * Format date : yyyy-MM-dd HH:mm:ss
 * Tags stockés comme "tag1,tag2" (virgule)
 */
public class CsvNoteDAO {
    private final Path cheminCSV;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String HEADER = "ID;TITLE;CONTENT;CREATION_DATE;MODIFICATION_DATE;TAGS";
    private static final String SEP = ";";

    public CsvNoteDAO(String cheminCSV) {
        this.cheminCSV = Paths.get(cheminCSV);
    }

    private void ensureParent() throws IOException {
        Path parent = cheminCSV.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    public synchronized void exporterCatalogue(Collection<Note> notes) throws IOException {
        ensureParent();
        // write to temp file then move
        Path tmp = Files.createTempFile("notes", ".csv.tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            w.write(HEADER);
            w.newLine();
            for (Note n : notes) {
                String[] parts = new String[6];
                parts[0] = n.getId() != null ? n.getId().toString() : "";
                parts[1] = escapeField(n.getTitre());
                parts[2] = escapeField(n.getContenu());
                parts[3] = n.getDateCreation() != null ? n.getFormattedCreationDate() : "";
                parts[4] = n.getDateModification() != null ? n.getFormattedModificationDate() : "";
                parts[5] = escapeField(n.getTagsAsString()); // tags as comma-separated
                w.write(String.join(SEP, parts));
                w.newLine();
            }
        }
        Files.move(tmp, cheminCSV, StandardCopyOption.REPLACE_EXISTING);
    }

    public synchronized List<Note> importer() throws IOException {
        if (!Files.exists(cheminCSV)) return Collections.emptyList();
        List<String> lines = Files.readAllLines(cheminCSV);
        List<Note> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i == 0) {
                // header validation is lenient
                if (!line.trim().equalsIgnoreCase(HEADER)) {
                    // possible different header: ignore but continue
                }
                continue;
            }
            if (line.trim().isEmpty()) continue;
            String[] parts = CSVUtils.splitCsvLine(line, ';'); // use semicolon
            if (parts.length < 6) continue;
            Note n = new Note();
            try {
                String idStr = parts[0].trim();
                if (!idStr.isEmpty()) n.setId(Long.valueOf(idStr));
                else n.setId(null);
            } catch (Exception e) { n.setId(null); }
            n.setTitre(unescape(parts[1]));
            n.setContenu(unescape(parts[2]));

            // dates
            try {
                String cdate = parts[3].trim();
                if (!cdate.isEmpty()) n.setDateCreation(LocalDateTime.parse(cdate, FMT));
            } catch (Exception ignored) {}
            try {
                String mdate = parts[4].trim();
                if (!mdate.isEmpty()) n.setDateModification(LocalDateTime.parse(mdate, FMT));
            } catch (Exception ignored) {}

            String tags = unescape(parts[5]).trim();
            if (!tags.isEmpty()) {
                n.setTagsFromString(tags); // expects comma separated
            }
            result.add(n);
        }
        return result;
    }

    private String escapeField(String s) {
        if (s == null) return "";
        // reuse CSVUtils.escapeForCsv but ensure we keep semicolon as separator
        return CSVUtils.escapeForCsv(s, ';');
    }

    private String unescape(String s) {
        return CSVUtils.unescapeFromCsv(s);
    }

    public boolean fileExists() {
        return Files.exists(cheminCSV);
    }

    public long getFileSize() throws IOException {
        return Files.size(cheminCSV);
    }
}
