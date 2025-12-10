package network;

import model.Note;

import java.io.*;
import java.net.Socket;

/**
 * Client simple : envoie Note et attend confirmation string "CONFIRMATION"
 */
public class NoteClient {
    private final String host;
    private final int port;

    public NoteClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Envoie une note au serveur et retourne true si confirmation reçue.
     */
    public boolean sendUpdate(Note note) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.writeObject(note);
            oos.flush();
            Object resp = ois.readObject();
            return resp != null && "CONFIRMATION".equals(resp.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
