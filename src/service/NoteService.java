package service;

import dao.CsvNoteDAO;
import dao.HibernateNoteDAO;
import model.Note;
import network.NoteClient;
import exceptions.InvalidNoteException;
import exceptions.NoteNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;


public class NoteService {
    private final List<Note> notesInMemory = new ArrayList<>();
    private final Map<String, List<Note>> indexParTag = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong idGen = new AtomicLong(1);

    private final CsvNoteDAO csvDao;
    private final HibernateNoteDAO hibernateDao;
    private final NoteClient networkClient;

    private final Set<Long> dirty = new HashSet<>();

    public NoteService(CsvNoteDAO csvDao, HibernateNoteDAO hibernateDao, NoteClient networkClient) {
        this.csvDao = csvDao;
        this.hibernateDao = hibernateDao;
        this.networkClient = networkClient;
    }


    public Note createNote(String titre, String contenu, Set<String> tags) {
        lock.lock();
        try {
            Note n = new Note();
            n.setId(idGen.getAndIncrement());
            n.setTitre(titre);
            n.setContenu(contenu);
            if (tags != null) n.setTags(tags);
            if (!validateNote(n)) throw new InvalidNoteException("Note invalide (titre obligatoire)");
            notesInMemory.add(n);
            indexNote(n);
            markDirty(n.getId());
            return n;
        } finally {
            lock.unlock();
        }
    }

    public void updateNote(Note note) {
        lock.lock();
        try {
            Optional<Note> opt = notesInMemory.stream().filter(n -> Objects.equals(n.getId(), note.getId())).findFirst();
            if (!opt.isPresent()) throw new NoteNotFoundException("Note introuvable: " + note.getId());
            Note existing = opt.get();
            existing.setTitre(note.getTitre());
            existing.setContenu(note.getContenu());
            existing.setTags(note.getTags());
            existing.touch();
            rebuildIndex();
            markDirty(existing.getId());
        } finally {
            lock.unlock();
        }
    }

    public void deleteNote(Long id) {
        lock.lock();
        try {
            boolean removed = notesInMemory.removeIf(n -> Objects.equals(n.getId(), id));
            if (!removed) throw new NoteNotFoundException("Note introuvable: " + id);
            rebuildIndex();
            markDirty(id);
        } finally {
            lock.unlock();
        }
    }

    public Stream<Note> getAllNotes() {
        lock.lock();
        try {
            return new ArrayList<>(notesInMemory).stream();
        } finally {
            lock.unlock();
        }
    }

    public Note getById(Long id) {
        lock.lock();
        try {
            return notesInMemory.stream().filter(n -> Objects.equals(n.getId(), id)).findFirst().orElse(null);
        } finally {
            lock.unlock();
        }
    }

    public Stream<Note> rechercherParMot(String mot) {
        lock.lock();
        try {
            String lower = mot == null ? "" : mot.toLowerCase();
            return notesInMemory.stream().filter(n ->
                    (n.getTitre() != null && n.getTitre().toLowerCase().contains(lower)) ||
                            (n.getContenu() != null && n.getContenu().toLowerCase().contains(lower)) ||
                            n.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lower))
            );
        } finally {
            lock.unlock();
        }
    }

    public Stream<Note> filterParDate(LocalDateTime debut, LocalDateTime fin) {
        lock.lock();
        try {
            return notesInMemory.stream().filter(n ->
                    (debut == null || !n.getDateCreation().isBefore(debut)) &&
                            (fin == null || !n.getDateCreation().isAfter(fin))
            );
        } finally {
            lock.unlock();
        }
    }

    public Stream<Note> trierParDate() {
        lock.lock();
        try {
            return notesInMemory.stream().sorted(Comparator.comparing(Note::getDateCreation));
        } finally {
            lock.unlock();
        }
    }

    public Stream<Note> trierParTitre() {
        lock.lock();
        try {
            return notesInMemory.stream().sorted(Comparator.comparing(Note::getTitre, Comparator.nullsFirst(String::compareTo)));
        } finally {
            lock.unlock();
        }
    }


    public boolean validateNote(Note note) {
        return note != null && note.getTitre() != null && !note.getTitre().trim().isEmpty();
    }

    private void markDirty(Long id) {
        if (id != null) dirty.add(id);
    }

    public boolean checkModification() {
        lock.lock();
        try {
            return !dirty.isEmpty();
        } finally {
            lock.unlock();
        }
    }


    public void savenote(Note note) {
        lock.lock();
        try {
            if (!validateNote(note)) throw new InvalidNoteException("Note invalide");
            Optional<Note> existing = notesInMemory.stream().filter(n -> Objects.equals(n.getId(), note.getId())).findFirst();
            if (existing.isPresent()) {
                Note ex = existing.get();
                ex.setTitre(note.getTitre());
                ex.setContenu(note.getContenu());
                ex.setTags(note.getTags());
                ex.touch();
            } else {
                notesInMemory.add(note);
                indexNote(note);
            }
            markDirty(note.getId());
        } finally {
            lock.unlock();
        }

        try {
            csvDao.exporterCatalogue(notesInMemory);
            try {
                hibernateDao.update(note);
            } catch (UnsupportedOperationException u) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                boolean ack = networkClient != null && networkClient.sendUpdate(note);
                if (ack) {
                    lock.lock();
                    try { dirty.remove(note.getId()); } finally { lock.unlock(); }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void indexNote(Note n) {
        if (n.getTags() != null) {
            for (String t : n.getTags()) {
                indexParTag.computeIfAbsent(t, k -> new ArrayList<>()).add(n);
            }
        }
    }

    private void rebuildIndex() {
        indexParTag.clear();
        for (Note n : notesInMemory) indexNote(n);
    }

    public List<Note> findByTag(String tag) {
        lock.lock();
        try {
            return indexParTag.getOrDefault(tag, Collections.emptyList());
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() { return lock; }
}
