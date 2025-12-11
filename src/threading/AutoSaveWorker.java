package threading;

import dao.CsvManager;
import service.NoteService;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoSaveWorker implements Runnable {
    private final NoteService service;
    private final CsvManager csvDao;
    private final long intervalMillis;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AutoSaveWorker(NoteService service, CsvManager csvDao, long intervalMillis) {
        this.service = service;
        this.csvDao = csvDao;
        this.intervalMillis = intervalMillis;
    }

    public void stop() { return;}

    @Override
    public void run() {
        return;
    }
}
