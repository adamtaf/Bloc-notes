package dao;

import model.Note;

import java.util.List;
import java.util.stream.Stream;

public class HibernateNoteDAO {

    public HibernateNoteDAO() {
    }

    public void save(Note note) {
        return;
    }

    public void saveAll(Stream<Note> notes) {
    }

    public void update(Note note) {
        return;
    }

    public void delete(Long id) {
        return;
    }

    public List<Note> findAll() {
        return null;
    }

    public List<Note> findByTag(String tag) {
        return null;
    }
}
