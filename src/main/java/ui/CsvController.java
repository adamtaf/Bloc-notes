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
        //pr ne choisir que les fichiers csv
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        //ouvrir la fenetre de dialogue pour choisir le fichier
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
        //nom par defaut du fichier a exporter on remplace tous les caracteres speciaux par _ et on ajoute .csv ala fin
        fileChooser.setInitialFileName(selected.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".csv");
        //filtrer pour choisir que les fichier csv
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(listeNotes.getScene().getWindow());
        //on verifie au cas ou l utilisateur annule l operation
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
        //on recupere la fenetre actuelle et on la supp
        Stage stage = (Stage) listeNotes.getScene().getWindow();
        stage.close(); // revient a l accueil
    }

    //afficher une alerte d info
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
