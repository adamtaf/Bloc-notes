package network;

import model.Note;

import java.io.*;
import java.net.Socket;


public class NoteClient {
    private final String host;
    private final int port;

    public NoteClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


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
