package threading;

import service.NoteService;
import dao.CsvNoteDAO;
import model.Note;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * AutoSave Worker : vérifie modifications et appelle savenote pour les notes dirty
 */
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
                    // snapshot all notes
                    List<Note> snapshot = service.getAllNotes().collect(Collectors.toList());
                    // write CSV
                    csvDao.exporterCatalogue(snapshot);
                    // for simplicity, call savenote on each dirty note to trigger DB/network per-note
                    // (efficient impl could batch update DB)
                    for (Note n : snapshot) {
                        // here we only call savenote if necessary: checkModification isn't per-note, so we call savenote for all
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
