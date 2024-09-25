import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class SocketListener implements Runnable {
    ServerSocket server;
    TopicManager topicManager;
    HashMap<Thread, Socket> children = new HashMap<>();  // Cambiato da ArrayList a HashMap

    public SocketListener(ServerSocket server, TopicManager topicManager) {
        this.server = server;
        this.topicManager = topicManager;
    }

    @Override
    public void run() {
        try {
            this.server.setSoTimeout(5000);

            while (!Thread.interrupted()) {
                try {
                    System.out.println("SocketListener: Waiting...");
                    Socket socket = this.server.accept();

                    if (!Thread.interrupted()) {
                        System.out.println("SocketListener: Client connected \n");

                        // Crea un nuovo thread per gestire il socket
                        Thread handlerThread = new Thread(new ClientHandler(socket, topicManager));
                        handlerThread.start();
                        
                        // Aggiungi il thread e il socket alla mappa
                        this.children.put(handlerThread, socket);
                    } else {
                        socket.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("SocketListener: Timeout, continuing...");
                    continue;
                } catch (IOException e) {
                    break;
                }
            }
            this.server.close();
        } catch (IOException e) {
            System.err.println("SocketListener: IOException caught: " + e);
            e.printStackTrace();
        }

        System.out.println("SocketListener: Interrupting children...");
        for (Thread child : this.children.keySet()) {
            System.out.println("Interrupting " + child + "...");
            
            // Ottieni il socket associato al thread
            Socket socket = this.children.get(child);

            // Invia il messaggio "quit" al client tramite il socket
            try {
                PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
                toClient.println("quit");
                toClient.close();
            } catch (IOException e) {
                System.err.println("Error sending 'quit' message to client: " + e.getMessage());
            }

            // Interrompi il thread
            child.interrupt();
        }
    }
}
