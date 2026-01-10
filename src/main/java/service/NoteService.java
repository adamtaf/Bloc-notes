package service;

import dao.CsvManager;
import dao.HibernateUtil;
import exceptions.CsvException;
import dao.HibernateNoteDAO;
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

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong idGen = new AtomicLong(1);

    private final CsvManager csvManager;
    private final HibernateNoteDAO hibernateDao;
    private final NoteClient networkClient;

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
        note.setTags(tags);

        Transaction tx = null; //pour stocker la transaction
        try (Session session = HibernateUtil.getSessionFactory().openSession()) { //on ouvre une session Hibernate
            tx = session.beginTransaction(); //on demarre une transaction
            session.persist(note); //comme un insert
            tx.commit(); //valider

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }

        try {
            csvManager.addNote(note);
        } catch (CsvException e) {
            e.printStackTrace();
        }

        markDirty();
        saveCsv();
        return note;
    }


    //read a note
    public Note getById(Long id) {
        return hibernateDao.getNote(id);
    }

    //get all notes
    public Stream<Note> getAllNotes() {
        return hibernateDao.findAll().stream();
    }


    public Note updateNote(Long id, String newTitle, String newContent, Set<String> newTags) throws Exception {
        Note existing = hibernateDao.getNote(id); //recupere la note de la bdd
        if (existing == null) throw new IllegalArgumentException("Note not found: " + id);

        if (newTitle != null) existing.setTitle(newTitle);
        if (newContent != null) existing.setContent(newContent);

        if (newTags != null) {
            existing.getTags().clear();
            existing.getTags().addAll(newTags);
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

    private volatile boolean needsSave = false; //pour savoir si on a des changement a sauvegarder ou nn
    //volatile pour que les changements soient visibles par les threads

    public void markDirty() {
        needsSave = true;
    } //pour sauvegarder les modifs

    public void saveOrUpdate(Note note) { //insert et update
        if (note == null) return;

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            //pour gerer les 2 cas note existante ou pas
            session.merge(note);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }

        saveCsv();
    }


    public Set<String> getAllTags() { //pour recuperer tous les tags existants
        return getAllNotes() //stream pour les notes
                .flatMap(n -> n.getTags().stream())
                //on prend le tag de chaque note et on les mets dans un seul flux(flatmap)
                .collect(Collectors.toSet()); //collecter les elements dans un set
    }


    public CsvManager getCsvManager() {
        return csvManager;
    }

    public boolean validateNote(Note note) {
        return true;
    }
}
