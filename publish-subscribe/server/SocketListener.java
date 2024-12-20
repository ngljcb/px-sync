import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class SocketListener implements Runnable {
    ServerSocket server;                                        // Il ServerSocket per ascoltare le connessioni in entrata
    TopicManager topicManager;                                  // Risorsa condivisa per gestire i topic
    ConcurrentHashMap<Thread, Socket> children;                 // Mappa di thread e socket associati ai client
    ConcurrentHashMap<Thread, ClientHandler> clientHandlers;    // Mappa di thread e ClientHandler associati ai client
    private volatile String inspectedTopic;                     // Variabile che indica se il TopicInspector è in esecuzione sul topic specificato

    /**
     * Costruttore per SocketListener.
     * 
     * @param server Il ServerSocket che accetta le connessioni client.
     * @param topicManager La risorsa condivisa TopicManager.
     */
    public SocketListener(ServerSocket server, TopicManager topicManager) {
        this.server = server;
        this.topicManager = topicManager;
        this.inspectedTopic = "";
        this.children = new ConcurrentHashMap<>();
        this.clientHandlers = new ConcurrentHashMap<>();
    }

    /**
     * Metodo run eseguito nel thread per accettare connessioni dai client e gestirle tramite thread separati.
     */
    @Override
    public void run() {
        try {
            // Imposta il timeout per accettare le connessioni
            this.server.setSoTimeout(5000);

            // Ciclo principale per accettare connessioni dai client
            while (!Thread.interrupted()) {
                try {
                    // Attende la connessione di un nuovo client
                    Socket socket = this.server.accept();

                    if (!Thread.interrupted()) {

                        // Crea un nuovo ClientHandler e il relativo thread
                        ClientHandler handler = new ClientHandler(socket, topicManager, inspectedTopic);
                        Thread handlerThread = new Thread(handler);
                        handlerThread.start();

                        // Aggiungi il ClientHandler alla mappa dei client
                        this.children.put(handlerThread, socket);

                        synchronized(this) {
                            this.clientHandlers.put(handlerThread, handler); // Mappa il ClientHandler
                        }
                        
                    } else {
                        socket.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    break;
                }
            }
            // Chiude il server quando esce dal ciclo principale
            this.server.close();
        } catch (IOException e) {
            System.err.println("SocketListener: IOException rilevata: " + e);
            e.printStackTrace();
        }

        // Procedura per interrompere tutti i thread figli e chiudere i rispettivi socket
        System.out.println("\n>>\n>>\n>>Interruzione dei client connessi... \n");
        for (Thread child : this.children.keySet()) {
            System.out.println("Interruzione del " + child + "...");

            // Ottiene il socket associato al thread
            Socket socket = this.children.get(child);

            // Invia il messaggio "quit" al client tramite il socket
            try {
                if (!socket.isClosed()) {
                    PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
                    toClient.println("quit");  // Invia il messaggio di chiusura al client
                    toClient.close();
                }
            } catch (IOException e) {
                System.err.println("Errore nell'invio del messaggio 'quit' al client: " + e.getMessage());
            }

            // Interrompe il thread
            child.interrupt();
        }
    }

    /**
     * Metodo per impostare la variabile `inspectorRunning` per tutti i ClientHandler attivi.
     * 
     * @param running Lo stato di `inspectorRunning` da impostare.
     */
    public synchronized void setInspectorRunningForAllClients(String topic) {
        this.inspectedTopic = topic;
        for (ClientHandler handler : this.clientHandlers.values()) {
            handler.setInspectorRunning(topic);  // Aggiorna lo stato di `inspectorRunning` per ciascun ClientHandler
        }
    }
}