package threading;

import dao.CsvManager;
import dao.HibernateNoteDAO;
import model.Note;
import service.NoteService;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoSaveWorker implements Runnable {
    private final NoteService service;
    private final HibernateNoteDAO hibernateDao;
    private final long intervalMillis;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AutoSaveWorker(NoteService service, HibernateNoteDAO hibernateDao, long intervalMillis) {
        this.service = service;
        this.hibernateDao = hibernateDao;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                for (Note note : service.getNotesObservable()) {
                    //verrouiller chaque note lors de la modif, afin qu un seul thread fasse des changements
                    note.getLock().lock();
                    try {
                        if (note.isDirty()) {
                            service.saveOrUpdate(note);  //sauvegarde dans hibernate et CSV
                            note.setDirty(false);
                        }
                    } finally {
                        note.getLock().unlock();        //liberer le lock
                    }
                }

                Thread.sleep(intervalMillis); //pause entre sauvegardes
            } catch (InterruptedException e) {
                break; //pour arreter le thread (interrompu par exp)
            }
        }
    }

    public void stop() { running.set(false);}




}
