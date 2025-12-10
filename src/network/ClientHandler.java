package network;

import java.io.*;
import java.net.Socket;

/**
 * Lit les messages entrants et renvoie une confirmation.
 */
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
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (running && !socket.isClosed()) {
                Object obj = in.readObject();
                // TODO: traiter différents types (Note, commandes, etc.)
                // Ici on répond simplement ACK
                out.writeObject("CONFIRMATION");
                out.flush();
            }
        } catch (EOFException eof) {
            // client closed
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
            server.removeHandler(this);
        }
    }

    public synchronized void envoyer(Object o) {
        try {
            if (out != null) {
                out.writeObject(o);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        running = false;
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }
}
