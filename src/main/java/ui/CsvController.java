package ui;

import exceptions.CsvException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Note;
import service.NoteService;

import java.io.File;
import java.util.List;

public class CsvController {

    @FXML private ListView<Note> listeNotes;
    @FXML private Button btnImportCsv;
    @FXML private Button btnExportCsv;
    @FXML private Button btnRetour;
    private NoteService service;
    private MainController mainController;


    public void init(NoteService service, MainController mainController) {
        this.service = service;
        listeNotes.getItems().setAll(service.getAllNotes().toList());
        this.mainController = mainController;

        btnImportCsv.setOnAction(e -> importerNote());
        btnExportCsv.setOnAction(e -> exporterNote());
        btnRetour.setOnAction(e -> retour());
    }

    private void importerNote() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer une note");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(listeNotes.getScene().getWindow());
        if (file == null) return;

        try {
            List<Note> notes = service.getCsvManager().importFromCsv(file.getAbsolutePath());

            if (!notes.isEmpty()) {
                Note note = notes.get(0);
                note.setId(null);

                service.saveOrUpdate(note);
                mainController.refreshNotes(service.getAllNotes().toList());

            }
            showInfo("Note importée avec succès !");
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }


    private void exporterNote() {
        Note selected = listeNotes.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la note");
        fileChooser.setInitialFileName(selected.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(listeNotes.getScene().getWindow());
        if (file != null) {
            try {
                service.getCsvManager().exportToCsv(List.of(selected), file.getAbsolutePath());
                showInfo("Note exportée avec succès !");
            } catch (CsvException e) {
                e.printStackTrace();
            }
        }
    }
    private void retour() {
        Stage stage = (Stage) listeNotes.getScene().getWindow();
        stage.close(); // revient a l accueil
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
