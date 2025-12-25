package network;

import model.Note;

public class NoteClient {
    private final String host;
    private final int port;

    public NoteClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public NoteClient() {
        host = "localhost";
        port = 8080;
    }


    public boolean sendUpdate(Note note) {
        return true;
    }
}
