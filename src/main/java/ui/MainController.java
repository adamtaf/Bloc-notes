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

        //chargement init des notes
        //on transforme le flux stream en ObservableList pour que la ListView soit reactive
        notesObservable = FXCollections.observableArrayList(
                service.getAllNotes().collect(Collectors.toList())
        );
        listeNotes.setItems(notesObservable);

        // Recherche dynamique
        //on filtre toutes les notes et on ne garde que celles avec le texte qu on veut
        recherche.textProperty().addListener((obs, oldText, newText) -> {
            notesObservable.setAll(
                    service.getAllNotes()
                            .filter(n -> n.getTitle() != null &&
                                    n.getTitle().toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toList())
            );
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


        service.loadNotes();
        listeNotes.setItems(service.getNotesObservable());

        //sauvegarde toutes les 1 secondes
        AutoSaveWorker worker = new AutoSaveWorker(service, hibernateDao, 1000);
        new Thread(worker).start();

    }


    @FXML
    public void onNouvelleNote() {
        //creee la note et la recupere
        Note note = service.createNote("Nouvelle note", "", new HashSet<>());

        if (note != null) {
            openEditor(note);
        }
    }


    @FXML
    public void onSauvegarde() throws CsvException {
        service.getCsvManager().writeAllNotes(service.getAllNotes().toList());
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
        notesObservable.setAll(
                service.getAllNotes()
                        .filter(n -> n.getTags().contains(tag))
                        .collect(Collectors.toList())
        );
    }

    @FXML
    private void onAfficherTout() {
        notesObservable.setAll(service.getAllNotes().collect(Collectors.toList()));
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

}