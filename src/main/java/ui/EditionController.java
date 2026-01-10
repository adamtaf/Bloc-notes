package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Note;
import service.NoteService;

public class EditionController {

    @FXML private TextField champTitre;
    @FXML private TextArea zoneContenu;
    @FXML private TextField champTag;
    @FXML private ListView<String> listeTags;

    private Note note;
    private NoteService service;




    public void init(Note note, NoteService service) {
        this.note = note;
        this.service = service;

        champTitre.setText(note.getTitle());
        zoneContenu.setText(note.getContent());

        listeTags.setItems(FXCollections.observableArrayList(service.getAllTags()));

        // clic sur le bouton, l ajout de la note
        listeTags.setOnMouseClicked(e -> {
            String tag = listeTags.getSelectionModel().getSelectedItem();
            if (tag != null) {
                champTag.setText(tag);
            }
        });

    }


    @FXML
    private void onSauvegarde() throws Exception {
        service.updateNote(note.getId(), champTitre.getText(), zoneContenu.getText(), note.getTags());
        listeTags.setItems(FXCollections.observableArrayList(service.getAllTags()));
    }


    @FXML
    private void onSupprimer() {
        try {
            service.deleteNote(note.getId());
            onRetour();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) champTitre.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAjouterTag() throws Exception {
        String nouveauTag = champTag.getText().trim();
        if (!nouveauTag.isEmpty()) {


            if (!note.getTags().contains(nouveauTag)) {
                note.getTags().add(nouveauTag);
            }

            if (!listeTags.getItems().contains(nouveauTag)) {
                listeTags.getItems().add(nouveauTag);
            }

            champTag.clear();
            onSauvegarde();
        }
    }

    @FXML
    private void onSupprimerTag() throws Exception {
        String tag = listeTags.getSelectionModel().getSelectedItem();
        if (tag != null) {
            note.getTags().remove(tag);
            onSauvegarde();
        }
    }


}
