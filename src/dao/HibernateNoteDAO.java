package dao;

import model.Note;

import java.util.List;

/**
 * Skeleton DAO for Hibernate. Implement using SessionFactory when ready.
 */
public class HibernateNoteDAO {

    public HibernateNoteDAO() {
        // TODO: init SessionFactory if needed
    }

    public void save(Note note) {
        throw new UnsupportedOperationException("Hibernate non configuré : implémenter save()");
    }

    public void update(Note note) {
        throw new UnsupportedOperationException("Hibernate non configuré : implémenter update()");
    }

    public void delete(Long id) {
        throw new UnsupportedOperationException("Hibernate non configuré : implémenter delete()");
    }

    public List<Note> findAll() {
        throw new UnsupportedOperationException("Hibernate non configuré : implémenter findAll()");
    }

    public List<Note> findByTag(String tag) {
        throw new UnsupportedOperationException("Hibernate non configuré : implémenter findByTag()");
    }
}
