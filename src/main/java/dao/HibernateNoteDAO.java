package dao;

import model.Note;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.List;
import java.util.stream.Stream;

public class HibernateNoteDAO {

    private SessionFactory sessionFactory;


    public HibernateNoteDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Note save(Note note) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(note);
        tx.commit();
        session.close();
        return note;
    }


    public List<Note> getAllNotes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Note", Note.class).list();
        }
    }

    public Note getNote(Long id) {
        Session session = sessionFactory.openSession();
        Note note = session.find(Note.class, id);
        session.close();
        return note;
    }

    public void saveAll(Stream<Note> notes) {
    }

    public void update(Note note) {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            session.merge(note);
            tx.commit();
            session.close();
        }

        public void delete(Long id) {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            Note note = session.find(Note.class, id);
            if (note != null) {
                session.remove(note);
            }
            tx.commit();
            session.close();
        }

        public List<Note> findAll() {
        Session session = sessionFactory.openSession();
        List<Note> notes = session.createQuery("from Note", Note.class).list();
        session.close();
        return notes;

    }

    public List<Note> findByTag(String tag) {
        return null;
    }
}
