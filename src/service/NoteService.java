package service;

import dao.CsvManager;
import exceptions.CsvException;
import dao.HibernateNoteDAO;
import model.Note;
import network.NoteClient;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

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


    public Note createNote(String titre, String contenu, Set<String> tags) {
        return null;
    }

    public void updateNote(Note updated) {
        return;
    }

    public void deleteNote(Long id) {
        return;
    }

    public Note getById(Long id) {
        return null;
    }

    public Stream<Note> getAllNotes() {
        return null;
    }


    private void saveCsv() {
        try {
            csvManager.writeAllNotes(notesInMemory);
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }

    public boolean validateNote(Note note) {
        return true;
    }
}
