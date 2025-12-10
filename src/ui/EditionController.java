package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Note;
import service.NoteService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Editeur de note : reçoit note et sauve via NoteService
 */
public class EditionController {
    @FXML private TextField champTitre;
    @FXML private TextArea champContenu;
    @FXML private TextField champTags;

    private Note noteActuelle;
    private NoteService service;

    public void setNoteAndService(Note note, NoteService service) {
        this.noteActuelle = note;
        this.service = service;
        if (note != null) {
            champTitre.setText(note.getTitre());
            champContenu.setText(note.getContenu());
            champTags.setText(String.join(";", note.getTags()));
        }
    }

    @FXML
    public void sauvegarder() {
        if (noteActuelle != null && service != null) {
            noteActuelle.setTitre(champTitre.getText());
            noteActuelle.setContenu(champContenu.getText());
            String tagsRaw = champTags.getText();
            Set<String> tags = new HashSet<>();
            if (tagsRaw != null && !tagsRaw.trim().isEmpty()) {
                Arrays.stream(tagsRaw.split(";")).map(String::trim).forEach(tags::add);
            }
            noteActuelle.setTags(tags);
            service.savenote(noteActuelle);
            // TODO : fermer fenêtre / rafraichir liste
        }
    }
}
