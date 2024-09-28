import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class SocketListener implements Runnable {
    ServerSocket server;                                // Il ServerSocket per ascoltare le connessioni in entrata
    TopicManager topicManager;                          // Risorsa condivisa per gestire i topic
    HashMap<Thread, Socket> children = new HashMap<>(); // Mappa di thread e socket associati ai client

    /**
     * Costruttore per SocketListener.
     * 
     * @param server Il ServerSocket che accetta le connessioni client.
     * @param topicManager La risorsa condivisa TopicManager.
     */
    public SocketListener(ServerSocket server, TopicManager topicManager) {
        this.server = server;
        this.topicManager = topicManager;
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
                    //System.out.println("SocketListener: In attesa di una connessione...");
                    Socket socket = this.server.accept();

                    // Se il thread non è interrotto, crea un nuovo thread per gestire il client
                    if (!Thread.interrupted()) {
                        System.out.println("SocketListener: Client connesso \n");

                        // Crea un nuovo thread per gestire il socket
                        Thread handlerThread = new Thread(new ClientHandler(socket, topicManager));
                        handlerThread.start();
                        
                        // Aggiunge il thread e il socket alla mappa dei client connessi
                        this.children.put(handlerThread, socket);
                    } else {
                        // Se il thread è interrotto, chiude il socket e esce
                        socket.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    // In caso di timeout, continua ad aspettare nuove connessioni
                    //System.out.println("SocketListener: Timeout, continuando...");
                    continue;
                } catch (IOException e) {
                    // Gestione delle eccezioni di input/output
                    break;
                }
            }
            // Chiude il server quando esce dal ciclo principale
            this.server.close();
        } catch (IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("SocketListener: IOException rilevata: " + e);
            e.printStackTrace();
        }

        // Procedura per interrompere tutti i thread figli e chiudere i rispettivi socket
        System.out.println("\n>>\n>>\n>>SocketListener: Interruzione dei client connessi... \n");
        for (Thread child : this.children.keySet()) {
            System.out.println("Interruzione del thread " + child + "...");

            // Ottiene il socket associato al thread
            Socket socket = this.children.get(child);

            // Invia il messaggio "quit" al client tramite il socket
            try {
                PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
                toClient.println("quit");  // Invia il messaggio di chiusura al client
                toClient.close();
            } catch (IOException e) {
                // Gestione dell'errore durante l'invio del messaggio di chiusura
                System.err.println("Errore nell'invio del messaggio 'quit' al client: " + e.getMessage());
            }

            // Interrompe il thread
            child.interrupt();
        }
    }
}