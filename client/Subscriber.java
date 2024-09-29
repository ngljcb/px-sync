import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Subscriber implements Runnable {
    private Socket socket;                      // Socket per la comunicazione con il server
    private String topic;                       // Il topic a cui il subscriber si è iscritto
    private volatile boolean quiting = false;   // Variabile condivisa per indicare quando il subscriber deve terminare

    /**
     * Costruttore della classe Subscriber.
     * 
     * @param socket Il socket per la comunicazione con il server.
     * @param topic Il topic a cui il subscriber si iscrive.
     */
    public Subscriber(Socket socket, String topic) {
        this.socket = socket;
        this.topic = topic; 
    }

    /**
     * Metodo eseguito nel thread per la gestione della sottoscrizione e della
     * comunicazione con il server.
     */
    @Override
    public void run() {
        // Oggetto che accetta input da tastiera
        Scanner userInput = new Scanner(System.in);
        try {
            // PrintWriter per inviare dati al server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);

            // Invia il comando di "subscribe" al server con il nome del topic
            toServer.println("subscribe " + this.topic);

            // Crea un thread separato per ricevere i messaggi in arrivo dal server
            Thread serverListener = new Thread(() -> {
                try {
                    // Scanner per ricevere i dati dal server
                    Scanner fromServer = new Scanner(this.socket.getInputStream());
                    
                    // Continua a leggere messaggi dal server finché il subscriber non termina
                    while (!quiting) {
                        
                        // Attende una risposta di conferma dal server
                        String response = fromServer.nextLine();

                        // Se il server invia il comando "quit", interrompe il ciclo
                        if (response.equals("quit") || this.socket.isClosed()) {
                            quiting = true;
                            break;
                        }
                        System.out.println(response);
                    }
                    // Chiude il canale di comunicazione con il server
                    fromServer.close();
                } catch (Exception e) {
                    // Gestione dell'eccezione se la connessione al server viene persa
                    System.out.println("T-sub: Connessione al server persa.");
                    System.out.println("T-sub: Comandi disponibili  >>  quit");
                }
            });

            // Avvia il thread per ascoltare i messaggi dal server
            serverListener.start();

            // Gestisce l'input dell'utente nel thread principale
            while (!quiting) {
                 // Aggiunta di un ritardo di 2 secondi tra ogni iterazione del ciclo
                try {
                    Thread.sleep(2000);  // Pausa di 2 secondi
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if (!socket.isClosed()) {
                    // Mostra all'utente come usare il client
                    System.out.println("\nComandi disponibili  >>  listall / quit");
                } else {
                    // Mostra all'utente come usare il client
                    System.out.println("\nComandi disponibili  >>  quit");
                }

                // Legge il comando dell'utente da tastiera
                String request = userInput.nextLine();

                // Comando "listall": richiede la lista di tutti i messaggi nel topic
                if (!socket.isClosed() && request.equals("listall")) {
                    toServer.println("listall " + this.topic);

                // Comando "quit": invia la richiesta di disconnessione al server
                } else if (request.equals("quit")) {
                    if (!socket.isClosed()) {
                        toServer.println("quit");
                    }
                    quiting = true;

                // Comando non riconosciuto
                } else {
                    System.out.println("Comando non riconosciuto. \n");
                }
            }

            // Attende la terminazione del thread che ascolta i messaggi dal server
            serverListener.join();

            // Chiude le risorse utilizzate per la comunicazione
            toServer.close();
            userInput.close();

        } catch (IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("IOException rilevata: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            // Gestione di eccezioni generali in caso di problemi di connessione
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione.");
        }
    }
}