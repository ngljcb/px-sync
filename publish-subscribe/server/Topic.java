import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Topic {

    private String name;                                    // Nome del topic
    private ConcurrentHashMap<ClientHandler, List<Message>> messages; // Risorsa condivisa per associare publisher ai loro messaggi
    private List<ClientHandler> subscribers;                // Lista dei subscriber
    private List<ClientHandler> publishers;                 // Lista dei publishers
    private int counter;                                    // Contatore per gli ID dei messaggi

    public Topic(String name) {
        this.name = name;
        this.messages = new ConcurrentHashMap<>();  // Inizializza la risorsa condivisa per i messaggi
        this.subscribers = new ArrayList<>();       // Inizializza la lista dei subscriber
        this.publishers = new ArrayList<>();        // Inizializza la lista dei publisher
        this.counter = 0;                           // Imposta il contatore dei messaggi a 0
    }

    /**
     * Aggiunge un nuovo messaggio da un publisher specifico.
     * 
     * @param text Il contenuto del messaggio.
     * @param publisher Il ClientHandler che ha inviato il messaggio.
     */
    public synchronized void addMessage(String text, ClientHandler publisher) {
        // Incrementa l'ID del messaggio
        this.counter++;
        
        // Verifica se il publisher ha già messaggi nella risorsa condivisa
        messages.computeIfAbsent(publisher, k -> new ArrayList<>())
                .add(new Message(counter, text, publisher));  // Aggiungi il messaggio al publisher
    }

    /**
     * Aggiunge un subscriber al topic.
     * 
     * @param subscriber Il ClientHandler che si vuole iscrivere al topic.
     */
    public synchronized void addSubscriber(ClientHandler subscriber) {
        subscribers.add(subscriber); // Aggiungi il subscriber alla lista
    }

    /**
     * Aggiunge un publisher al topic.
     * 
     * @param publisher Il ClientHandler che si vuole registrare come publisher.
     */
    public synchronized void addPublisher(ClientHandler publisher) {
        publishers.add(publisher); // Aggiungi il publisher alla lista
    }

    /**
     * Restituisce la lista dei messaggi associati a un publisher specifico.
     * 
     * @param publisher Il ClientHandler di cui si vogliono ottenere i messaggi.
     * @return La lista di messaggi associata al publisher, o null se non esistono.
     */
    public synchronized List<Message> getMessagesByPublisher(ClientHandler publisher) {
        return this.messages.get(publisher); // Ritorna i messaggi del publisher
    }

    /**
     * Restituisce tutti i messaggi nel topic, raggruppati per publisher.
     * 
     * @return La risorsa condivisa contenente i messaggi per ogni publisher.
     */
    public synchronized ConcurrentHashMap<ClientHandler, List<Message>> getAllMessages() {
        return this.messages; // Ritorna tutti i messaggi associati ai publisher
    }

    /**
     * Restituisce la lista di tutti i subscriber registrati a questo topic.
     * 
     * @return La lista dei subscriber.
     */
    public synchronized List<ClientHandler> getSubscribers() {
        return this.subscribers; // Ritorna la lista dei subscriber
    }

    /**
     * Restituisce una lista di tutti i messaggi nel topic, indipendentemente dal publisher.
     * 
     * @return Una lista contenente tutti i messaggi nel topic.
     */
    public synchronized List<Message> getAllMessagesAsList() {
        List<Message> allMessages = new ArrayList<>();
        // Itera attraverso tutte le liste di messaggi dei publisher
        for (List<Message> messageList : messages.values()) {
            // Aggiungi tutti i messaggi alla lista finale
            allMessages.addAll(messageList);
        }
        return allMessages; // Ritorna la lista di tutti i messaggi
    }

    /**
     * Elimina un messaggio dal topic in base al suo ID.
     * 
     * @param id L'ID del messaggio da eliminare.
     * @return true se il messaggio è stato trovato e cancellato, false altrimenti.
     */
    public synchronized boolean deleteMessageById(int id) {
        // Itera attraverso tutte le liste di messaggi dei publisher
        for (List<Message> messageList : messages.values()) {
            Iterator<Message> iterator = messageList.iterator();
            // Cerca il messaggio con l'ID specificato
            while (iterator.hasNext()) {
                Message message = iterator.next();
                if (message.getid() == id) {
                    // Se trovato, rimuove il messaggio
                    iterator.remove();
                    return true; // Conferma che il messaggio è stato eliminato
                }
            }
        }
        return false; // Nessun messaggio trovato con l'ID dato
    }
}