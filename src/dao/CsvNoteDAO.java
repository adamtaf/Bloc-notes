package dao;

import model.Note;
import utils.CSVUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV DAO simple.
 * CSV header: id,titre,contenu,dateCreation,dateModification,tags
 */
public class CsvNoteDAO {
    private final Path cheminCSV;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CsvNoteDAO(String cheminCSV) {
        this.cheminCSV = Paths.get(cheminCSV);
    }

    public synchronized void exporterCatalogue(Collection<Note> notes) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id,titre,contenu,dateCreation,dateModification,tags");
        for (Note n : notes) {
            String tags = String.join(";", n.getTags());
            String line = String.format("%d,%s,%s,%s,%s,%s",
                    n.getId() == null ? 0L : n.getId(),
                    CSVUtils.escapeForCsv(n.getTitre()),
                    CSVUtils.escapeForCsv(n.getContenu()),
                    n.getDateCreation() == null ? "" : n.getDateCreation().format(FMT),
                    n.getDateModification() == null ? "" : n.getDateModification().format(FMT),
                    CSVUtils.escapeForCsv(tags));
            lines.add(line);
        }
        Files.createDirectories(cheminCSV.getParent() == null ? Paths.get(".") : cheminCSV.getParent());
        Files.write(cheminCSV, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public synchronized List<Note> importer() throws IOException {
        if (!Files.exists(cheminCSV)) return Collections.emptyList();
        List<String> lines = Files.readAllLines(cheminCSV);
        List<Note> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = CSVUtils.splitCsvLine(lines.get(i));
            if (parts.length < 6) continue;
            Note n = new Note();
            try { n.setId(Long.valueOf(parts[0])); } catch (Exception e) { n.setId(null); }
            n.setTitre(CSVUtils.unescapeFromCsv(parts[1]));
            n.setContenu(CSVUtils.unescapeFromCsv(parts[2]));
            String tags = CSVUtils.unescapeFromCsv(parts[5]);
            if (!tags.isEmpty()) n.setTags(Arrays.stream(tags.split(";")).collect(Collectors.toSet()));
            result.add(n);
        }
        return result;
    }
}
