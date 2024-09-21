import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SocketListener implements Runnable {
    ServerSocket server;
    TopicManager topicManager;
    ArrayList<Thread> children = new ArrayList<>();

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
                    /*
                     * Questa istruzione è bloccante, a prescindere da Thread.interrupt(). Occorre
                     * quindi controllare, una volta accettata la connessione, che il server non sia
                     * stato interrotto.
                     * 
                     * In caso venga raggiunto il timeout, viene sollevata una
                     * SocketTimeoutException, dopo la quale potremo ricontrollare lo stato del
                     * Thread nella condizione del while().
                     */
                    Socket socket = this.server.accept();
                    if (!Thread.interrupted()) {
                        System.out.println("SocketListener: Client connected \n");

                        /* crea un nuovo thread per lo specifico socket */
                        Thread handlerThread = new Thread(new ClientHandler(socket, topicManager));
                        handlerThread.start();
                        this.children.add(handlerThread);
                        // /*
                        //  * una volta creato e avviato il thread, torna in ascolto per il prossimo client
                        //  */
                        // try {
                        //     handlerThread.join();
                        //     break;
                        // } catch (InterruptedException e) {
                        //     //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        //     return;
                        // }
                    } else {
                        socket.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    /* in caso di timeout procediamo semplicemente con l'esecuzione */
                    System.out.println("SocketListener: Timeout, continuing...");
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

        System.out.println("SocketListener: Interrupting children...");
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