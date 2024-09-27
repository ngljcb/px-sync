import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

public class ShowTopics implements Runnable {
    private Socket socket;          // Socket per la comunicazione con il server
    private CountDownLatch latch;   // Latch per sincronizzare con il thread principale

    /**
     * Costruttore della classe ShowTopics.
     * 
     * @param socket Il socket per la comunicazione con il server.
     * @param latch Il CountDownLatch per sincronizzare con il thread Client.
     */
    public ShowTopics(Socket socket, CountDownLatch latch) {
        this.socket = socket;
        this.latch = latch;  // Inizializza il latch per la sincronizzazione
    }

    /**
     * Metodo eseguito nel thread per richiedere e mostrare i topic dal server.
     */
    @Override
    public void run() {
        try {
            // PrintWriter per inviare la richiesta "show" al server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
            toServer.println("show");

            // Scanner per ricevere i dati dal server
            Scanner fromServer = new Scanner(this.socket.getInputStream());
            String response;

            // Stampa l'intestazione per la lista dei topic
            System.out.println("Elenco dei topic disponibili: ");
            
            // Continua a leggere e stampare i topic finché non riceve il segnale di fine ("SHOWTOPICbreak")
            while (fromServer.hasNextLine()) {
                response = fromServer.nextLine();
                if (response.equals("SHOWTOPICbreak")) {
                    break; // Termina la lettura quando il server invia il segnale di fine
                } else {
                    System.out.println(response); // Stampa il topic ricevuto
                }
            }

            // Chiude il canale di comunicazione con il server
            fromServer.close();
            toServer.close();

            // Segnala al thread principale che il lavoro è stato completato
            latch.countDown(); 

        } catch (IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("IOException rilevata: " + e);
            e.printStackTrace();
        }
    }
}