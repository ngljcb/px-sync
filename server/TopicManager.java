import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TopicManager {

    // Risorsa condivisa che associa i nomi dei topic agli oggetti Topic
    private HashMap<String, Topic> topics;

    public TopicManager() {
        // Inizializza la risorsa condivisa dei topic
        this.topics = new HashMap<>();
    }

    /**
     * Aggiunge un publisher a un topic specifico. Se il topic non esiste, ne crea uno nuovo.
     * 
     * @param publisher Il ClientHandler che rappresenta il publisher.
     * @param topicName Il nome del topic a cui aggiungere il publisher.
     */
    public synchronized void addPublisher(ClientHandler publisher, String topicName) {
        if (!topics.containsKey(topicName)) {
            // Se il topic non esiste, viene creato e il publisher è associato ad esso
            Topic newTopic = new Topic(topicName);
            newTopic.addPublisher(publisher);

            // Aggiunge il nuovo topic alla risorsa condivisa
            topics.put(topicName, newTopic);
        } else {
            // Se il topic esiste, viene aggiunto il publisher esistente
            Topic topic = topics.get(topicName);
            topic.addPublisher(publisher);
        }
    }

    /**
     * Aggiunge un subscriber a un topic specifico. Se il topic non esiste, ne crea uno nuovo.
     * 
     * @param subscriber Il ClientHandler che rappresenta il subscriber.
     * @param topicName Il nome del topic a cui aggiungere il subscriber.
     */
    public synchronized void addSubscriber(ClientHandler subscriber, String topicName) {
        if (!topics.containsKey(topicName)) {
            // Se il topic non esiste, viene creato e il subscriber è associato ad esso
            Topic newTopic = new Topic(topicName);
            newTopic.addSubscriber(subscriber);

            // Aggiunge il nuovo topic alla risorsa condivisa
            topics.put(topicName, newTopic);
        } else {
            // Se il topic esiste, viene aggiunto il subscriber esistente
            Topic topic = topics.get(topicName);
            topic.addSubscriber(subscriber);
        }
    }

    /**
     * Pubblica un messaggio su un topic specifico e ritorna la lista di subscriber associati a quel topic.
     * 
     * @param publisher Il ClientHandler che rappresenta il publisher.
     * @param topicName Il nome del topic su cui pubblicare il messaggio.
     * @param message Il contenuto del messaggio da pubblicare.
     * @return La lista dei subscriber associati al topic.
     */
    public synchronized List<ClientHandler> publishMessage(ClientHandler publisher, String topicName, String message) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            // Aggiunge il messaggio al topic e lo associa al publisher
            topic.addMessage(message, publisher);
        }

        // Ritorna la lista di subscriber per quel topic
        return topic.getSubscribers();
    }

    /**
     * Ritorna una lista di messaggi di un publisher specifico per un determinato topic.
     * 
     * @param publisher Il ClientHandler che rappresenta il publisher.
     * @param topicName Il nome del topic da cui ottenere i messaggi.
     * @return Un Optional contenente una lista di messaggi, o vuoto se il topic non esiste.
     */
    public synchronized Optional<List<Message>> listMessages(ClientHandler publisher, String topicName) {
        // Ottiene il topic in modo sicuro tramite Optional e ritorna i messaggi associati al publisher
        return Optional.ofNullable(topics.get(topicName))
                       .map(topic -> topic.getMessagesByPublisher(publisher)); // Se il topic esiste, ottiene i messaggi
    }

    /**
     * Ritorna una lista di tutti i messaggi per un topic specifico.
     * 
     * @param topicName Il nome del topic da cui ottenere i messaggi.
     * @return Un Optional contenente una lista di tutti i messaggi del topic, o vuoto se il topic non esiste.
     */
    public synchronized Optional<List<Message>> listMessagesByTopic(String topicName) {
        // Ottiene il topic in modo sicuro tramite Optional
        return Optional.ofNullable(topics.get(topicName))
                       .map(topic -> {
                           // Ottiene la mappa dei messaggi associati a ciascun ClientHandler
                           HashMap<ClientHandler, List<Message>> messagesMap = topic.getAllMessages();

                           // Combina tutte le liste di messaggi in una singola lista e la ritorna
                           return messagesMap.values().stream()
                                             .flatMap(List::stream)
                                             .toList();  // Ritorna una lista singola con tutti i messaggi
                       });
    }

    /**
     * Metodo per ottenere una lista di nomi di tutti i topic presenti.
     * 
     * @return Una lista contenente i nomi di tutti i topic.
     */
    public synchronized List<String> getTopicNames() {
        // Ritorna una lista contenente le chiavi (i nomi dei topic) dalla risorsa condivisa
        return new ArrayList<>(topics.keySet());
    }

    /**
     * Ritorna un Optional contenente il topic se esiste, altrimenti un Optional vuoto.
     * 
     * @param topicName Il nome del topic da cercare.
     * @return Un Optional contenente il topic se esiste, altrimenti vuoto.
     */
    public synchronized Optional<Topic> getTopicByName(String topicName) {
        // Usa Optional per restituire il topic, gestendo in modo sicuro l'assenza del topic
        return Optional.ofNullable(topics.get(topicName));
    }
}
