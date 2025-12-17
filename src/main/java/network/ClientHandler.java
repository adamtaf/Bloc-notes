package network;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private final Socket socket;
    private final NoteServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, NoteServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        return;
    }

    public synchronized void envoyer(Object o) {
        return;
    }

    public void close() {
        return;
    }
}
