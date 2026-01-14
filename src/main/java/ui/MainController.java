package ui;

import exceptions.CsvException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Note;
import service.NoteService;
import dao.CsvManager;
import dao.HibernateNoteDAO;
import network.NoteClient;
import threading.AutoSaveWorker;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;



public class MainController {

    @FXML private ListView<Note> listeNotes;
    @FXML private TextField recherche;


    private NoteService service;
    private ObservableList<Note> notesObservable;


    public void initialize() throws CsvException {

        CsvManager csvManager = new CsvManager("notes.csv");
        HibernateNoteDAO hibernateDao = new HibernateNoteDAO();
        NoteClient networkClient = new NoteClient();
        service = new NoteService(csvManager, hibernateDao, networkClient);

        //initialisation de l'observable list
        notesObservable = FXCollections.observableArrayList();
        listeNotes.setItems(notesObservable);

        //charger toutes les notes
        service.loadNotes();
        notesObservable.setAll(service.getAllNotes().toList());

        // Recherche dynamique
        //on filtre toutes les notes et on ne garde que celles avec le texte qu on veut
        recherche.textProperty().addListener((obs, oldText, newText) -> {
            List<Note> filtered = service.getAllNotes().toList().stream()
                    .filter(n -> n.getTitle() != null &&
                            n.getTitle().toLowerCase().contains(newText.toLowerCase()))
                    .collect(Collectors.toList());

            notesObservable.setAll(filtered);
        });

        //double clique sur une note pour l editer
        listeNotes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                //on prend la note
                Note note = listeNotes.getSelectionModel().getSelectedItem();
                if (note != null) {
                    //on ouvre l editeur
                    openEditor(note);
                }
            }
        });

        //sauvegarde toutes les 1 secondes
        AutoSaveWorker worker = new AutoSaveWorker(service, hibernateDao, 1000);
        new Thread(worker).start();

    }


    @FXML
    public void onNouvelleNote() {
        //creee la note et la recupere et le hashset vide pour les tags et le contenu vide
        Note note = service.createNote("Nouvelle note", "", new HashSet<>());

        if (note != null) {
            showInfo("Nouvelle note créée avec succès !");
            openEditor(note);
        }
    }


    @FXML
    public void onSauvegarde() throws CsvException {
        service.getCsvManager().writeAllNotes(service.getAllNotes().toList());
        showInfo("Auto-save actif. Pas besoin de sauvegarder manuellement.");
    }


    private void openEditor(Note note) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/editor.fxml"));
            Scene scene = new Scene(loader.load());

            EditionController controller = loader.getController();
            controller.init(note, service);

            Stage stage = (Stage) listeNotes.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filterByTag(String tag) {
        List<Note> filtered = service.getAllNotes().toList().stream()
                .filter(n -> n.getTags().contains(tag))
                .collect(Collectors.toList());

        notesObservable.setAll(filtered);
    }



    @FXML
    private void onAfficherTout() {
        notesObservable.setAll(service.getAllNotes().toList());
    }

    @FXML
    private void onOpenFilterScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/tags.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());

        FilterController controller = loader.getController();
        controller.init(service, this);

        stage.setScene(scene);
        stage.setTitle("Filtrer par tags");
        stage.show();
    }


    @FXML
    private void onOpenImportExportScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/csv.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());

        CsvController controller = loader.getController();
        controller.init(service, this);

        stage.setScene(scene);
        stage.setTitle("Import / Export CSV");
        stage.show();
    }


    public void refreshNotes(List<Note> nouvellesNotes) {
        notesObservable.setAll(nouvellesNotes);
    }


    //alertes
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