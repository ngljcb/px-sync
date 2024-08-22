import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SocketListener implements Runnable {
    ServerSocket server;
    ArrayList<Thread> children = new ArrayList<>();

    public SocketListener(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            this.server.setSoTimeout(5000);
            Resource r = new Resource(); // risorsa condivisa tra tutti i thread dei vari client

            while (!Thread.interrupted()) {
                try {
                    System.out.println("Waiting for a new client...");
                    /*
                     * Questa istruzione è bloccante, a prescindere da Thread.interrupt(). Occorre
                     * quindi controllare, una volta accettata la connessione, che il server non sia
                     * stato interrotto.
                     * 
                     * In caso venga raggiunto il timeout, viene sollevata una
                     * SocketTimeoutException, dopo la quale potremo ricontrollare lo stato del
                     * Thread nella condizione del while().
                     */
                    Socket s = this.server.accept();
                    if (!Thread.interrupted()) {
                        System.out.println("Client connected");

                        /* crea un nuovo thread per lo specifico socket */
                        Thread handlerThread = new Thread(new ClientHandler(s, r));
                        handlerThread.start();
                        this.children.add(handlerThread);
                        /*
                         * una volta creato e avviato il thread, torna in ascolto per il prossimo client
                         */
                    } else {
                        s.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    /* in caso di timeout procediamo semplicemente con l'esecuzione */
                    System.out.println("Timeout, continuing...");
                    continue;
                } catch (IOException e) {
                    /*
                     * s.close() potrebbe sollevare un'eccezione; in questo caso non vogliamo finire
                     * nel "catch" esterno, perché non abbiamo ancora chiamato this.server.close()
                     */
                    break;
                }
            }
            this.server.close();
        } catch (IOException e) {
            System.err.println("SocketListener: IOException caught: " + e);
            e.printStackTrace();
        }

        System.out.println("Interrupting children...");
        for (Thread child : this.children) {
            System.out.println("Interrupting " + child + "...");
            /*
             * child.interrupt() non è bloccante; una volta inviato il segnale
             * di interruzione proseguiamo con l'esecuzione, senza aspettare che "child"
             * termini
             */
            child.interrupt();
        }

    }
}