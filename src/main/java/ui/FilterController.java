package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Note;
import service.NoteService;

import java.util.Set;
import java.util.stream.Collectors;

public class FilterController {

    @FXML private ListView<String> listeTags;

    private NoteService service;
    private MainController mainController;

    public void init(NoteService service, MainController mainController) {
        this.service = service;
        this.mainController = mainController;

        Set<String> tags = service.getAllNotes()
                .flatMap(n -> n.getTags().stream())
                .collect(Collectors.toSet());

        ObservableList<String> tagsObs = FXCollections.observableArrayList(tags);
        listeTags.setItems(tagsObs);

        listeTags.setOnMouseClicked(e -> {
            String tag = listeTags.getSelectionModel().getSelectedItem();
            if (tag != null) {
                mainController.filterByTag(tag);
                onRetour();
            }
        });

    }


    @FXML
    private void onRetour() {
        Stage stage = (Stage) listeTags.getScene().getWindow();
        stage.close();
    }
}
