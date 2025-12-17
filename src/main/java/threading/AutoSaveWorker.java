package threading;

import dao.CsvManager;
import dao.HibernateNoteDAO;
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
                hibernateDao.saveAll(service.getAllNotes());


                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    public void stop() { running.set(false);}




}
