import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Publisher implements Runnable {
    private Socket socket;                      // Socket per la comunicazione con il server
    private String topic;                       // Il topic su cui pubblicare i messaggi
    private volatile boolean quiting = false;   // Variabile condivisa per indicare se il publisher sta terminando

    /**
     * Costruttore della classe Publisher.
     * 
     * @param socket Il socket per la comunicazione con il server.
     * @param topic Il topic su cui il publisher invierà messaggi.
     */
    public Publisher(Socket socket, String topic) {
        this.socket = socket;
        this.topic = topic;
    }

    /**
     * Metodo eseguito nel thread per la gestione della pubblicazione e della
     * comunicazione con il server.
     */
    @Override
    public void run() {
        // Oggetto per leggere l'input da tastiera dell'utente
        Scanner userInput = new Scanner(System.in);
        try {
            // PrintWriter per inviare dati al server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);

            // Invia il comando di "publish" al server con il nome del topic
            toServer.println("publish " + this.topic);

            // Crea un thread separato per ascoltare i messaggi in arrivo dal server
            Thread serverListener = new Thread(() -> {
                try {
                    // Scanner per ricevere i dati dal server
                    Scanner fromServer = new Scanner(this.socket.getInputStream());

                    // Attende una risposta di conferma dal server
                    String response = fromServer.nextLine();
                    System.out.println("Server response: " + response);

                    // Continua a leggere messaggi dal server finché il publisher non termina
                    while (!quiting) {
                        // Legge il prossimo messaggio dal server
                        response = fromServer.nextLine();
                        System.out.println(response);

                        // Se il server invia il comando "quit", interrompe il ciclo
                        if (response.equals("quit")) {
                            System.out.println("Server has disconnected.");
                            quiting = true;
                            break;
                        }
                    }
                    // Chiude il canale di comunicazione con il server
                    fromServer.close();
                } catch (Exception e) {
                    // Gestione dell'eccezione se la connessione al server viene persa
                    System.out.println("Connection to server lost.");
                }
            });

            // Avvia il thread per ascoltare i messaggi dal server
            serverListener.start();

            // Gestisce l'input dell'utente nel thread principale
            while (!quiting) {
                // Legge il comando dell'utente da tastiera
                String request = userInput.nextLine();

                // Comando "send": invia un messaggio al topic specificato
                if (request.startsWith("send ")) {
                    String message = request.substring(5); // Estrae il testo dopo "send "
                    toServer.println("send " + this.topic + " " + message);

                // Comando "list": richiede la lista dei messaggi del publisher
                } else if (request.equals("list")) {
                    toServer.println("list " + this.topic);

                // Comando "listall": richiede la lista di tutti i messaggi nel topic
                } else if (request.equals("listall")) {
                    toServer.println("listall " + this.topic);

                // Comando "quit": invia la richiesta di disconnessione al server
                } else if (request.equals("quit")) {
                    toServer.println("quit");
                    quiting = true;

                // Comando non riconosciuto
                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }

            // Attende la terminazione del thread che ascolta i messaggi dal server
            serverListener.join();

            // Chiude le risorse utilizzate per la comunicazione
            toServer.close();
            userInput.close();

        } catch (IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            // Gestione di eccezioni generali in caso di problemi di connessione
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione.");
        }
    }
}