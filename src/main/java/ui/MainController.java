package ui;

import exceptions.CsvException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Note;
import service.NoteService;
import dao.CsvManager;
import dao.HibernateNoteDAO;
import network.NoteClient;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {

    @FXML private ListView<Note> listeNotes;
    @FXML private TextArea zoneContenu;
    @FXML private TextField recherche;
    @FXML private Button btnImportCsv;
    @FXML private Button btnExportCsv;

    private NoteService service;
    private ObservableList<Note> notesObservable;

    public void initialize() throws CsvException {
        // Initialisation des dépendances
        CsvManager csvManager = new CsvManager("notes.csv"); // fichier CSV local
        HibernateNoteDAO hibernateDao = new HibernateNoteDAO();
        NoteClient networkClient = new NoteClient();

        service = new NoteService(csvManager, hibernateDao, networkClient);

        // Récupérer les notes existantes
        notesObservable = FXCollections.observableArrayList(
                service.getAllNotes().collect(Collectors.toList())
        );
        listeNotes.setItems(notesObservable);

        // Gestion de la sélection d'une note
        listeNotes.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if (newNote != null) {
                zoneContenu.setText(newNote.getContent());
            } else {
                zoneContenu.clear();
            }
        });

        //douaa
        // Mise à jour automatique du contenu
        zoneContenu.textProperty().addListener((obs, oldText, newText) -> {
            Note note = listeNotes.getSelectionModel().getSelectedItem();
            if (note != null) {
                note.setContent(newText);
                try {
                    service.updateNote(note.getId(), note.getTitle(), note.getContent(), note.getTags());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Recherche dynamique
        recherche.textProperty().addListener((obs, oldText, newText) -> {
            notesObservable.setAll(
                    service.getAllNotes()
                            .filter(n -> n.getTitle() != null && n.getTitle().toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toList())
            );
        });

        // Gestion Import/Export CSV
        btnImportCsv.setOnAction(e -> importCsv());
        btnExportCsv.setOnAction(e -> exportCsv());
    }


    @FXML
    public void onNouvelleNote() {
        Set<String> tags = new HashSet<>();
        Note noteSauvegardee = service.createNote("Nouvelle note", "", tags);

        notesObservable.add(noteSauvegardee); // ajoute la note persistée avec son ID
        listeNotes.getSelectionModel().select(noteSauvegardee);
    }

    @FXML
    public void onSauvegarde() throws Exception {
        Note note = listeNotes.getSelectionModel().getSelectedItem();
        if (note != null) {
            service.updateNote(note.getId(), note.getTitle(), note.getContent(), note.getTags());
        }
    }

    @FXML
    public void onSupprimer() throws Exception {
        Note note = listeNotes.getSelectionModel().getSelectedItem();
        if (note != null) {
            service.deleteNote(note.getId());
            notesObservable.remove(note);
            zoneContenu.clear();
        }
    }


    // --- Méthodes Import/Export CSV ---
    @FXML
    private void importCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(listeNotes.getScene().getWindow());
        if (file != null) {
            try {
                List<Note> importedNotes = service.getCsvManager().importFromCsv(file.getAbsolutePath());
                notesObservable.setAll(importedNotes);
            } catch (CsvException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void exportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les notes vers CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(listeNotes.getScene().getWindow());
        if (file != null) {
            try {
                service.getCsvManager().exportToCsv(notesObservable, file.getAbsolutePath());
            } catch (CsvException e) {
                e.printStackTrace();
            }
        }
    }
}
