import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.Optional;

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

        System.out.println("\nAvviato il server. \n\n");

        // Ottiene la porta dalla riga di comando
        int port = Integer.parseInt(args[0]);
        Scanner userInput = new Scanner(System.in);

        try {
            // Crea un TopicManager condiviso tra tutti i thread
            TopicManager topicManager = new TopicManager(); 

            // Avvia il server sulla porta specificata
            ServerSocket server = new ServerSocket(port);

            // Crea e avvia il thread SocketListener per gestire i client
            SocketListener socketListener = new SocketListener(server, topicManager); 
            Thread serverThread = new Thread(socketListener);
            serverThread.start();

            String command = "";

            // Flag per terminare il ciclo
            boolean quitting = false;

            // Ciclo principale che accetta i comandi dall'utente
            while (!quitting) {
                System.out.println("Comandi disponibili  >>  inspect / show / quit");

                command = userInput.nextLine();

                // Se l'utente inserisce "show", avvia un thread per estrarre tutti i topic
                if (command.startsWith("show")) {
                    Thread topicExtractor = new Thread(new TopicExtractor(topicManager));
                    topicExtractor.start();
                    try {
                        // Attende che il thread TopicExtractor termini
                        topicExtractor.join();
                    } catch (InterruptedException e) {
                        return;
                    }

                // Se l'utente inserisce "inspect", avvia un thread per ispezionare un topic
                } else if (command.equals("inspect")) {
                    
                    System.out.println("\nInserisci il topic da ispezionare:");
                    String topicName = userInput.nextLine();

                    Optional<Topic> optionalTopic = topicManager.getTopicByName(topicName);

                    if (optionalTopic.isEmpty()) {
                        System.out.println("Errore: Topic non trovato. \n");
                    } else {
                        
                        CountDownLatch latch = new CountDownLatch(1);
    
                        // Prima di eseguire l'ispezione, segnala a tutti i ClientHandler che l'ispezione è in corso
                        socketListener.setInspectorRunningForAllClients(topicName);
    
                        Thread topicInspector = new Thread(new TopicInspector(topicManager, latch, topicName));
                        topicInspector.start();
    
                        try {
                            // Attende che TopicInspector completi il suo lavoro
                            latch.await();
                            topicInspector.join();
                        } catch (InterruptedException e) {
                            return;
                        }
    
                        topicName = "";
    
                        // Dopo che l'ispezione è completata, riprende l'esecuzione normale dei ClientHandler
                        socketListener.setInspectorRunningForAllClients(topicName);
                        System.out.println("Riprendi i ClientHandler, ispezione terminata.");
                    }

                // Se l'utente inserisce "quit", esce dal ciclo principale
                } else if (command.startsWith("quit")) {
                    quitting = true;

                // Se il comando non è riconosciuto, stampa un messaggio di errore
                } else {
                    System.out.println("Comando sconosciuto. \n");
                }
            }

            // Interrompe il thread che gestisce le connessioni client e attende la sua terminazione
            try {
                serverThread.interrupt();
                serverThread.join();
            } catch (InterruptedException e) {
                return;
            }
            System.out.println("\n\n>> Server terminato.");
        } catch (IOException e) {
            System.err.println("IOException rilevata: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }
}