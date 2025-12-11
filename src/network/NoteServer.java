package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class NoteServer {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = false;

    public void demarrer(int port) throws IOException {
        return;
    }

    public void diffuser(Object message) {
        return;
    }

    public void removeHandler(ClientHandler handler) {
        return;
    }

    public void stop() throws IOException {
        return;
    }
}
