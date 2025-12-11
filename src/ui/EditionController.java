package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Note;
import service.NoteService;

public class EditionController {

    @FXML private TextField champTitre;
    @FXML private TextArea champContenu;
    @FXML private TextField champTags;

    private Note noteActuelle;
    private NoteService service;

    public void setNoteAndService(Note note, NoteService service) {
        this.noteActuelle = note;
        this.service = service;

    }

    @FXML
    public void sauvegarder() {
        return;
    }
}
