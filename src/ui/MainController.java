package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Note;
import dao.CsvNoteDAO;
import dao.HibernateNoteDAO;
import network.NoteClient;
import service.NoteService;

import java.util.HashSet;


public class MainController {
    @FXML private ListView<Note> listeNotes;
    @FXML private TextArea zoneContenu;
    @FXML private TextField recherche;

    private NoteService service;

    public void initialize() {
        // initialisations : créer DAOs, network client, service
        CsvNoteDAO csvDao = new CsvNoteDAO("data/notes.csv");
        HibernateNoteDAO hibernateDao = new HibernateNoteDAO();
        NoteClient client = new NoteClient("localhost", 9000);
        service = new NoteService(csvDao, hibernateDao, client);

    }

    @FXML
    public void onNouvelleNote() {
        Note n = service.createNote("Nouvelle note", "", new HashSet<>());
    }

    @FXML
    public void onSauvegarde() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setContenu(zoneContenu.getText());
            service.savenote(selected);
        }
    }

    @FXML
    public void onSupprimer() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.deleteNote(selected.getId());
        }
    }

    @FXML
    public void onRechercher() {
        String mot = recherche.getText();
    }
}
