package service;

import dao.CsvManager;
import dao.HibernateUtil;
import exceptions.CsvException;
import dao.HibernateNoteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Note;
import network.NoteClient;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class NoteService {


    private final CsvManager csvManager;
    private final HibernateNoteDAO hibernateDao;
    private final NoteClient networkClient;
    //pour l interface et la notifier automatiques des changements de valeurs
    private final ObservableList<Note> notesObservable = FXCollections.observableArrayList();


    public NoteService(CsvManager csvManager, HibernateNoteDAO hibernateDao, NoteClient networkClient) {
        this.csvManager = csvManager;
        this.hibernateDao = hibernateDao;
        this.networkClient = networkClient;
    }


//create a note
    public Note createNote(String title, String content, Set<String> tags) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setTags(new HashSet<>(tags));

        Transaction tx = null;
        Note savedNote = null;

        //session Hibernate(canal de communication avec la bdd qui va etre ouvert et lire ajouter des donner en bdd...) pour creer ou mettre a jour la note
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        tx = session.beginTransaction();

        // merge gere les nouvelles notes et celles deja existantes
            //met a jour et pas seulement creer
        savedNote = (Note) session.merge(note);

        tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return null;
        }
        //sauvegarde dans le CSV
        try {
            csvManager.addNote(savedNote);
        } catch (CsvException e) {
            e.printStackTrace();
        }

        //pour marquer la note comme modifiee
        markDirty();
        //pour mettre a jour la liste
        notesObservable.add(savedNote);
        //sauveg du csv
        saveCsv();

        return savedNote;
    }


    //read a note
    public Note getById(Long id) {
        return hibernateDao.getNote(id);
    }

    //get all notes
    public Stream<Note> getAllNotes() {
        return notesObservable.stream();
    }
    public ObservableList<Note> getNotesObservable() {
        return notesObservable;
    }


    public Note updateNote(Long id, String newTitle, String newContent, Set<String> newTags) throws Exception {
        Note existing = hibernateDao.getNote(id); //recupere la note de la bdd
        if (existing == null) throw new IllegalArgumentException("Note not found: " + id);

        if (newTitle != null) existing.setTitle(newTitle);
        if (newContent != null) existing.setContent(newContent);

        if (newTags != null) {
            existing.getTags().clear();
            existing.getTags().addAll(new HashSet<>(newTags));

        } //remplacer les anciens tags avc les nvs

        existing.touch(); //met a jour la date de la derniere date de modif
        hibernateDao.update(existing);
        markDirty(); //rafraichissement de l interface ou une sauveg
        saveCsv(); //sauveg du csv
        return existing; //return la note mise a jour
    }



    // delete a note
    public void deleteNote(Long id) throws Exception {
        saveCsv();
        markDirty();
        hibernateDao.delete(id);
        notesObservable.removeIf(n -> n.getId().equals(id));
    }

    public boolean hasChanges() { //pour verifier si on a u des modifs apres la sauvegarde
        return needsSave;
    }

    public void saveCsv() {
        try {
            List<Note> notes = hibernateDao.findAll();  //ls donnees de la bdd
            csvManager.writeAllNotes(notes);

            needsSave = false; //tt est a jour
            System.out.println("Auto-save OK");
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }
    public void loadNotes() {
        notesObservable.clear();
        notesObservable.addAll(hibernateDao.getAllNotes());
    }


    private volatile boolean needsSave = false; //pour savoir si on a des changement a sauvegarder ou nn
    //volatile pour que les changements soient visibles par les threads
    //chaque thread pourrait garder sa propre copie de needsSave et ca poserait pb donc on le met en volatile qui va permettre a tt les threads de voir la meme valeur en temps reel

    public void markDirty() {
        needsSave = true;
    } //pour sauvegarder les modifs

    public void saveOrUpdate(Note note) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Note managed;

            if (note.getId() == null) {
                managed = (Note) session.merge(note);
                notesObservable.add(managed);
            } else {
                managed = session.find(Note.class, note.getId());
                if (managed != null) {
                    managed.setTitle(note.getTitle());
                    managed.setContent(note.getContent());
                    managed.getTags().clear();
                    managed.getTags().addAll(note.getTags());
                    managed.touch();
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }

        saveCsv();
    }


    public Set<String> getAllTags() { //pour recuperer tous les tags existants
        return getAllNotes() //stream pour les notes
                .flatMap(n -> n.getTags().stream())
                //on prend le tag de chaque note et on les mets dans un seul flux(flatmap) car on obtient beacoup de stream don on les regrp en un
                .collect(Collectors.toSet()); //collecter les elements dans un set
    }


    public CsvManager getCsvManager() {
        return csvManager;
    }

    public boolean validateNote(Note note) {
        return true;
    }
}
