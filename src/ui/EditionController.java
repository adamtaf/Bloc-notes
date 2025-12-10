package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Note;
import service.NoteService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
            // display tags as comma-separated for consistency with CSV
            champTags.setText(note.getTagsAsString().replace(",", ";")); // friendly: show ; in UI
        }
    }

    @FXML
    public void sauvegarder() {
        if (noteActuelle != null && service != null) {
            noteActuelle.setTitre(champTitre.getText());
            noteActuelle.setContenu(champContenu.getText());
            String tagsRaw = champTags.getText();
            // accept both ; and , as separators, normalize to comma for storage
            if (tagsRaw == null) tagsRaw = "";
            String normalized = tagsRaw.replace(';', ',');
            // use Note helper to set tags from comma-separated string
            noteActuelle.setTagsFromString(normalized);
            service.savenote(noteActuelle);
            // TODO: fermer fenêtre / rafraichir liste (géré par caller)
        }
    }
}
