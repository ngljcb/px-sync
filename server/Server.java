import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {

    /**
     * Metodo principale per avviare il server e gestire le connessioni client.
     * Accetta i comandi dall'utente e avvia thread separati per gestire i topic.
     *
     * @param args Argomenti da linea di comando, dove args[0] è la porta del server.
     */
    public static void main(String[] args) {
        // Controlla che venga fornita la porta del server come argomento
        if (args.length < 1) {
            System.err.println("Uso corretto: java Server <port>");
            return;
        }

        // Ottiene la porta dalla riga di comando
        int port = Integer.parseInt(args[0]);
        Scanner userInput = new Scanner(System.in);

        try {
            // Crea un TopicManager condiviso tra tutti i thread
            TopicManager topicManager = new TopicManager(); 

            // Avvia il server sulla porta specificata
            ServerSocket server = new ServerSocket(port);

            /*
             * Avvia un thread separato per gestire le connessioni dei client.
             * Il thread principale ascolta i comandi da tastiera dell'utente per la gestione del server.
             */
            Thread serverThread = new Thread(new SocketListener(server, topicManager));
            serverThread.start();

            String command = "";

            // Ciclo principale che accetta i comandi dall'utente
            while (!command.equals("quit")) {
                command = userInput.nextLine();

                // Se l'utente inserisce "show", avvia un thread per estrarre tutti i topic
                if (command.startsWith("show")) {
                    Thread topicExtractor = new Thread(new TopicExtractor(topicManager));
                    topicExtractor.start();
                    try {
                        // Attende che il thread TopicExtractor termini
                        topicExtractor.join();
                    } catch (InterruptedException e) {
                        // Se il thread viene interrotto, termina il server
                        return;
                    }

                // Se l'utente inserisce "inspect", avvia un thread per ispezionare un topic
                } else if (command.startsWith("inspect")) {
                    Thread topicInspector = new Thread(new TopicInspector(topicManager));
                    topicInspector.start();
                    try {
                        // Attende che il thread TopicInspector termini
                        topicInspector.join();
                    } catch (InterruptedException e) {
                        // Se il thread viene interrotto, termina il server
                        return;
                    }

                // Se il comando non è riconosciuto, stampa un messaggio di errore
                } else {
                    System.out.println("Comando sconosciuto");
                }
            }

            // Interrompe il thread che gestisce le connessioni client e attende la sua terminazione
            try {
                serverThread.interrupt();
                /* Attende la terminazione del thread SocketListener */
                serverThread.join();
            } catch (InterruptedException e) {
                // Se viene interrotto, termina il server
                return;
            }
            System.out.println("Thread principale terminato.");
        } catch (IOException e) {
            // Gestione dell'eccezione di input/output
            System.err.println("IOException rilevata: " + e);
            e.printStackTrace();
        } finally {
            // Chiude l'input da tastiera
            userInput.close();
        }
    }
}