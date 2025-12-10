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

/**
 * Controller principal minimal : crée, sauvegarde, supprime, recherche.
 */
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

        // TODO : charger metadata CSV dans listeNotes (service.getAllNotes)
    }

    @FXML
    public void onNouvelleNote() {
        Note n = service.createNote("Nouvelle note", "", new HashSet<>());
        // TODO : rafraîchir UI et sélectionner la note
    }

    @FXML
    public void onSauvegarde() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setContenu(zoneContenu.getText());
            service.savenote(selected); // conforme au diagramme de séquence
            // TODO : rafraîchir UI
        }
    }

    @FXML
    public void onSupprimer() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.deleteNote(selected.getId());
            // TODO : rafraîchir UI
        }
    }

    @FXML
    public void onRechercher() {
        String mot = recherche.getText();
        // TODO : utiliser service.rechercherParMot(mot) et afficher résultats
    }
}
