package ui;


import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Note;
import service.NoteService;


public class MainController {

    @FXML private ListView<Note> listeNotes;
    @FXML private TextArea zoneContenu;
    @FXML private TextField recherche;

    private NoteService service;

    public void initialize() {
        return;
    }

    @FXML
    public void onNouvelleNote() {
        return;
    }

    @FXML
    public void onSauvegarde() {
        return;
    }

    @FXML
    public void onSupprimer() {
        return;
    }
}
