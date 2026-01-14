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

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;


import java.util.HashSet;

public class EditionController {

    @FXML private TextField champTitre;
    @FXML private TextArea zoneContenu;
    @FXML private TextField champTag;
    @FXML private ListView<String> listeTags;

    private Note currentNote;
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
        champTitre.textProperty().addListener((obs, oldV, newV) -> {
            note.setTitle(newV);
        });

        zoneContenu.textProperty().addListener((obs, oldV, newV) -> {
            note.setContent(newV);
        });


    }


    @FXML
    private void onSauvegarde() throws Exception {
        service.updateNote(note.getId(), champTitre.getText(), zoneContenu.getText(), note.getTags());
        listeTags.setItems(FXCollections.observableArrayList(service.getAllTags()));
        showInfo("Auto-save actif. Pas besoin de sauvegarder manuellement.");
    }


    @FXML
    private void onSupprimer() {
        boolean confirm = showConfirmation("Voulez-vous vraiment supprimer cette note ?");
        if (!confirm) return;

        try {
            service.deleteNote(note.getId());
            showInfo("Note supprimée avec succès !");
            onRetour();
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Erreur lors de la suppression !");
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
                note.setTags(new HashSet<>(note.getTags()));
                note.getTags().add(nouveauTag);
                note.setDirty(true);
            }

            if (!listeTags.getItems().contains(nouveauTag)) {
                listeTags.getItems().add(nouveauTag);
            }

            champTag.clear();
            service.markDirty(); // pour signaler qu'il y a un changement

        }
    }

    @FXML
    private void onSupprimerTag() throws Exception {
        String tag = listeTags.getSelectionModel().getSelectedItem();
        if (tag != null) {
            note.getTags().remove(tag);
            service.markDirty(); // pour signaler qu'il y a un changement

        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Action requise");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


}
