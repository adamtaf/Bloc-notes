package threading;

import service.NoteService;
import dao.CsvNoteDAO;
import model.Note;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class AutoSaveWorker implements Runnable {
    private final NoteService service;
    private final CsvNoteDAO csvDao;
    private final long intervalMillis;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AutoSaveWorker(NoteService service, CsvNoteDAO csvDao, long intervalMillis) {
        this.service = service;
        this.csvDao = csvDao;
        this.intervalMillis = intervalMillis;
    }

    public void stop() { running.set(false); }

    @Override
    public void run() {
        while (running.get()) {
            try {
                if (service.checkModification()) {
                    List<Note> snapshot = service.getAllNotes().collect(Collectors.toList());
                    csvDao.exporterCatalogue(snapshot);

                    for (Note n : snapshot) {
                        service.savenote(n);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try { Thread.sleep(intervalMillis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }
}
