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
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class NoteService {

    private final List<Note> notesInMemory = new ArrayList<>();
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



//create a note //douaa
    public Note createNote(String title, String content, Set<String> tags) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(note);
            tx.commit();
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
        Note existing = hibernateDao.getNote(id);
        if (existing == null) throw new IllegalArgumentException("Note not found: " + id);

        if (newTitle != null) existing.setTitle(newTitle);
        if (newContent != null) existing.setContent(newContent);
        if (newTags != null) existing.setTags(newTags);

        existing.touch();
        hibernateDao.update(existing);
        return existing;
    }

    // delete a note
    public void deleteNote(Long id) throws Exception {
        hibernateDao.delete(id);
    }


    private void saveCsv() {
        try {
            csvManager.writeAllNotes(notesInMemory);
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }

    public CsvManager getCsvManager() {
        return csvManager;
    }

    public boolean validateNote(Note note) {
        return true;
    }
}
