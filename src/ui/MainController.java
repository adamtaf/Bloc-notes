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
import java.util.List;

/**
 * Controller principal minimal : crée, sauvegarde, supprime, recherche.
 */
public class MainController {
    @FXML private ListView<Note> listeNotes;
    @FXML private TextArea zoneContenu;
    @FXML private TextField recherche;

    private NoteService service;
    private CsvNoteDAO csvDao;

    public void initialize() {
        // initialisations : créer DAOs, network client, service
        csvDao = new CsvNoteDAO("data/notes.csv");
        HibernateNoteDAO hibernateDao = new HibernateNoteDAO();
        NoteClient client = new NoteClient("localhost", 9000);
        service = new NoteService(csvDao, hibernateDao, client);

        // charger depuis CSV si présent
        try {
            List<Note> loaded = csvDao.importer();
            service.loadFromCsv(loaded);
            // remplir la liste UI
            listeNotes.getItems().clear();
            service.getAllNotes().forEach(n -> listeNotes.getItems().add(n));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO : lier sélection pour afficher contenu
        listeNotes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                zoneContenu.setText(newV.getContenu());
            } else {
                zoneContenu.clear();
            }
        });
    }

    @FXML
    public void onNouvelleNote() {
        Note n = service.createNote("Nouvelle note", "", new HashSet<>());
        // rafraîchir UI et sélectionner la note
        listeNotes.getItems().add(n);
        listeNotes.getSelectionModel().select(n);
    }

    @FXML
    public void onSauvegarde() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setContenu(zoneContenu.getText());
            service.savenote(selected); // conforme au diagramme de séquence
            // rafraîchir l'affichage (ListView se mettra à jour si toString change)
            listeNotes.refresh();
        }
    }

    @FXML
    public void onSupprimer() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.deleteNote(selected.getId());
            listeNotes.getItems().remove(selected);
        }
    }

    @FXML
    public void onRechercher() {
        String mot = recherche.getText();
        listeNotes.getItems().clear();
        service.rechercherParMot(mot).forEach(n -> listeNotes.getItems().add(n));
    }
}
