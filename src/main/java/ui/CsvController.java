package ui;

import exceptions.CsvException;
import javafx.fxml.FXML;
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(listeNotes.getScene().getWindow());
        if (file != null) {
            try {
                List<Note> notes = service.getCsvManager().importFromCsv(file.getAbsolutePath());
                if (!notes.isEmpty()) {
                    Note note = notes.get(0); // prend la premiere note du fichier
                    service.saveOrUpdate(note);
                    listeNotes.getItems().add(note);
                }
            } catch (CsvException e) {
                e.printStackTrace();
            }
        }
        mainController.refreshNotes(service.getAllNotes().toList());
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
            } catch (CsvException e) {
                e.printStackTrace();
            }
        }
    }
    private void retour() {
        Stage stage = (Stage) listeNotes.getScene().getWindow();
        stage.close(); // revient a l accueil
    }
}
