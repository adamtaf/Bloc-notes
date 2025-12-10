package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Serveur qui accepte clients et diffuse mises à jour.
 */
public class NoteServer {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = false;

    public void demarrer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        while (running) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, this);
            synchronized (clients) { clients.add(handler); }
            new Thread(handler).start();
        }
    }

    public void diffuser(Object message) {
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.envoyer(message);
            }
        }
    }

    public void removeHandler(ClientHandler handler) {
        synchronized (clients) { clients.remove(handler); }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) serverSocket.close();
        synchronized (clients) {
            for (ClientHandler c : clients) c.close();
            clients.clear();
        }
    }
}
