import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    Socket socket;          // Socket per la comunicazione con il client
    TopicManager resource;  // Risorsa condivisa per gestire i topic

    /**
     * Costruttore della classe ClientHandler.
     * 
     * @param socket Il socket che rappresenta la connessione con il client.
     * @param resource La risorsa condivisa TopicManager che gestisce i topic.
     */
    public ClientHandler(Socket socket, TopicManager resource) {
        this.socket = socket;
        this.resource = resource;
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

            // Messaggio di avvio thread
            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            boolean closed = false; // Flag per terminare il ciclo
            while (!closed) {
                // Ricezione di una richiesta dal client
                String requestLine = fromClient.nextLine();
                String[] parts = requestLine.split(" ", 3);
                String request = parts[0];            // Tipo di richiesta (es. "publish", "subscribe", ecc.)
                String topic = parts.length > 1 ? parts[1] : "";  // Nome del topic (se presente)
                String message = parts.length > 2 ? parts[2] : "";  // Messaggio (se presente)

                if (!Thread.interrupted()) {
                    // Log della richiesta ricevuta
                    System.out.println("ClientHandler: tipo di richiesta: " + request);
                    switch (request) {
                        case "quit":
                            // Gestione del comando "quit"
                            closed = true;
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
                                toClient.println("ClientHandler: Subscriber aggiunto al topic " + topic);
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
                                        toClient.println("Messaggi:");
                                        messages.forEach(toClient::println); // Stampa ogni messaggio
                                    },
                                    () -> toClient.println("Sono stati inviati 0 messaggi per il topic " + topic + ".") // Se non ci sono messaggi
                                );
                                toClient.println("ClientHandler: Elenco dei messaggi per il topic " + topic);
                            } else {
                                toClient.println("ClientHandler: Comando sconosciuto.");
                            }
                            break;

                        case "listall":
                            // Elenca tutti i messaggi del topic, indipendentemente dal publisher
                            if (parts.length > 1) {
                                Optional<List<Message>> optionalMessages = this.resource.listMessagesByTopic(topic);
                                optionalMessages.ifPresentOrElse(
                                    messages -> {
                                        toClient.println("Sono stati inviati " + messages.size() + " messaggi per il topic " + topic + "."); 
                                        messages.forEach(toClient::println); // Stampa ogni messaggio
                                    },
                                    () -> toClient.println("Sono stati inviati 0 messaggi per il topic " + topic + ".")
                                );
                                toClient.println("ClientHandler: Elenco di tutti i messaggi per il topic " + topic);
                            } else {
                                toClient.println("ClientHandler: Comando sconosciuto.");
                            }
                            break;

                        case "send":
                            // Invia un messaggio a tutti i subscriber di un topic
                            if (parts.length > 1) {
                                List<ClientHandler> subs = this.resource.publishMessage(this, topic, message);
                                
                                subs.forEach(s -> {
                                    try {
                                        PrintWriter sender = new PrintWriter(s.socket.getOutputStream(), true);
                                        sender.println(message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                toClient.println("ClientHandler: Il publisher ha inviato un messaggio al topic " + topic);

                            } else {
                                toClient.println("ClientHandler: Comando sconosciuto.");
                            }
                            break;

                        case "show":
                            // Mostra tutti i topic disponibili
                            List<String> topicNames = this.resource.getTopicNames();
                            topicNames.forEach(toClient::println);
                            toClient.println("SHOWTOPICbreak");
                            break;

                        default:
                            // Gestione di comando sconosciuto
                            toClient.println("ClientHandler: Comando sconosciuto.");
                    }
                } else {
                    break;
                }
            }

            // Invia il messaggio di chiusura al client
            toClient.println("quit");

            // Chiude le risorse
            toClient.close();
            fromClient.close();
            this.socket.close();
            System.out.println("Connessione chiusa.");

        } catch(IOException e) {
            // Gestione delle eccezioni di input/output
            System.err.println("ClientHandler: IOException rilevata: " + e);
            e.printStackTrace();
        } catch(Exception e){
            // Gestione di altre eccezioni
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione."); 
        }
    }
}