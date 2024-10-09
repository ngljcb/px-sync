import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {

    Socket socket;                          // Socket per la comunicazione con il client
    TopicManager resource;                  // Risorsa condivisa per gestire i topic
    BlockingQueue<String> requestQueue;     // Coda per salvare le richieste in arrivo
    private volatile boolean inspectorRunning;  // Variabile che indica se il TopicInspector è in esecuzione

    /**
     * Costruttore della classe ClientHandler.
     * 
     * @param socket Il socket che rappresenta la connessione con il client.
     * @param resource La risorsa condivisa TopicManager che gestisce i topic.
     */
    public ClientHandler(Socket socket, TopicManager resource, boolean inspectorRunning) {
        this.socket = socket;
        this.resource = resource;
        this.requestQueue = new LinkedBlockingQueue<>();  // Inizializza la coda delle richieste
        this.inspectorRunning = inspectorRunning;         // Inizialmente il TopicInspector non è in esecuzione
    }

    /**
     * Metodo per impostare lo stato di esecuzione del TopicInspector.
     */
    public synchronized void setInspectorRunning(boolean running) {
        this.inspectorRunning = running;
        if (!running) {
            notifyAll(); // Risveglia tutti i thread in attesa quando inspectorRunning diventa false
        }
    }

    /**
     * Metodo eseguito nel thread per gestire la comunicazione con il client.
     */
    @Override
    public void run() {
        try {
            // Scanner per ricevere i dati dal client
            Scanner fromClient = new Scanner(this.socket.getInputStream());
            // PrintWriter per inviare i dati al client
            PrintWriter toClient = new PrintWriter(this.socket.getOutputStream(), true);

            if(!inspectorRunning) {
                // Messaggio di avvio thread
                System.out.println("\n"+ Thread.currentThread() + " in ascolto...\n");
                System.out.println("Comandi disponibili  >>  inspect / show / quit");
            }

            // Usa un array booleano per gestire la chiusura
            final boolean[] closed = {false};

            // Thread separato per leggere le richieste dal client e inserirle nella coda
            Thread requestReader = new Thread(() -> {
                try {
                    while (!closed[0] && fromClient.hasNextLine()) {
                        String requestLine = fromClient.nextLine();
                        requestQueue.put(requestLine);  // Aggiunge la richiesta alla coda
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Reimposta il flag di interruzione
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            requestReader.start(); // Avvia il thread che legge dal client

            while (!closed[0] && !Thread.currentThread().isInterrupted()) {
                try {
                    
                    // Ottiene la prossima richiesta dalla coda solo se il TopicInspector non è in esecuzione
                    String requestLine = requestQueue.take();
                    String[] parts = requestLine.split(" ", 3);
                    String request = parts[0];            // Tipo di richiesta (es. "publish", "subscribe", ecc.)
                    String topic = parts.length > 1 ? parts[1] : "";  // Nome del topic (se presente)
                    String message = parts.length > 2 ? parts[2] : "";  // Messaggio (se presente)
                    
                    // Sincronizzazione prima di eseguire il blocco switch
                    synchronized (this) {
                        while (inspectorRunning) {
                            //System.out.println("Il thread è in attesa, inspectorRunning: " + inspectorRunning);
                            wait();  // Attende finché l'ispezione non termina
                        }

                        // Log della richiesta ricevuta
                        System.out.println("\nArrivato una richiesta: " + request);
                        System.out.println("Comandi disponibili  >>  inspect / show / quit");

                        // Switch sincronizzato
                        switch (request) {
                            case "quit":
                                // Gestione del comando "quit"
                                closed[0] = true;
                                System.out.println(this.toString() + " sta terminando...");
                                break;

                            case "publish":
                                // Aggiunge un publisher al topic
                                if (parts.length > 1) {                                
                                    this.resource.addPublisher(this, topic);
                                    toClient.println("Publisher aggiunto al topic " + topic);
                                } else {
                                    toClient.println("Nessuna chiave specificata.");
                                }
                                break;

                            case "subscribe":
                                // Aggiunge un subscriber al topic
                                if (parts.length > 1) {
                                    this.resource.addSubscriber(this, topic);
                                    toClient.println("Subscriber aggiunto al topic " + topic);
                                } else {
                                    toClient.println("Nessuna chiave specificata.");
                                }
                                break;

                            case "list":
                                // Elenca i messaggi per un topic specifico
                                if (parts.length > 1) {
                                    Optional<List<Message>> optionalMessages = this.resource.listMessages(this, topic);
                                    optionalMessages.ifPresentOrElse(
                                        messages -> {
                                            toClient.println("\nMessaggi:");
                                            messages.forEach(toClient::println); // Stampa ogni messaggio
                                        },
                                        () -> toClient.println("Sono stati inviati 0 messaggi per il topic " + topic + ". \n") // Se non ci sono messaggi
                                    );
                                } else {
                                    toClient.println("Comando sconosciuto. \n");
                                }
                                break;

                            case "listall":
                                // Elenca tutti i messaggi del topic, indipendentemente dal publisher
                                if (parts.length > 1) {
                                    Optional<List<Message>> optionalMessages = this.resource.listMessagesByTopic(topic);
                                    optionalMessages.ifPresentOrElse(
                                        messages -> {
                                            toClient.println("Sono stati inviati " + messages.size() + " messaggi per il topic " + topic + ". \n"); 
                                            messages.forEach(toClient::println); // Stampa ogni messaggio
                                        },
                                        () -> toClient.println("Sono stati inviati 0 messaggi per il topic " + topic + ". \n")
                                    );
                                } else {
                                    toClient.println("Comando sconosciuto. \n");
                                }
                                break;

                            case "send":
                                // Invia un messaggio a tutti i subscriber di un topic
                                if (parts.length > 1) {
                                    List<ClientHandler> subs = this.resource.publishMessage(this, topic, message);
                                    
                                    subs.forEach(s -> {
                                    // Sincronizza l'invio del messaggio per ogni subscriber
                                    synchronized (s) {
                                        try {
                                            PrintWriter sender = new PrintWriter(s.socket.getOutputStream(), true);
                                            sender.println("\nMessaggio per il topic " + topic + ": " + message);
                                            sender.println("\n\nComandi disponibili  >>  listall / quit");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    });
                                    
                                } else {
                                    toClient.println("Comando sconosciuto. \n");
                                }
                                break;

                            case "show":
                                // Mostra tutti i topic disponibili
                                List<String> topicNames = this.resource.getTopicNames();
                                
                                if(topicNames.size() == 0) {
                                    toClient.println("Attualmente non ci sono topic disponibili. \n");
                                } else {
                                    // Stampa tutti i topic ottenuti
                                    toClient.println("Topics:");
                                    topicNames.forEach(t -> toClient.println(" - " + t));
                                    toClient.println("\n");
                                }
                                toClient.println("SHOWTOPICbreak");
                                break;

                            default:
                                // Gestione di comando sconosciuto
                                toClient.println("Comando sconosciuto. \n");
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Reimposta il flag di interruzione
                    break;  // Uscita dal ciclo
                }
            }

            if(!this.socket.isClosed()) {
                // Invia il messaggio di chiusura al client
                toClient.println("quit");
            }

            // Chiude le risorse
            toClient.close();
            fromClient.close();
            this.socket.close();

        } catch(IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("ClientHandler: Errore rilevato: " + e);
            e.printStackTrace();
        } catch(Exception e){
            System.out.println("Errore durante la gestione della connessione: " + e.getMessage());
        }
    }
}