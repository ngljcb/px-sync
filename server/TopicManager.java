import java.util.HashMap;
import java.util.ArrayList;
// import java.util.Scanner;
import java.util.List;
import java.util.Optional;

public class TopicManager {
    private HashMap<String, Topic> topics;

    public TopicManager() {
        this.topics = new HashMap<>();
    }

    public synchronized void addPublisher(ClientHandler publisher, String topicName) {
        if(!topics.containsKey(topicName)){
            Topic newTopic = new Topic(topicName);
            newTopic.addPublisher(publisher);
            topics.put(topicName, newTopic);
        } else {
            Topic topic = topics.get(topicName);
            topic.addPublisher(publisher);
        }
    }

    public synchronized void addSubscriber(ClientHandler subscriber, String topicName) {
        if(!topics.containsKey(topicName)){
            Topic newTopic = new Topic(topicName);
            newTopic.addSubscriber(subscriber);
            topics.put(topicName, newTopic);
        } else {
            Topic topic = topics.get(topicName);
            topic.addSubscriber(subscriber);
        }
    }

    public synchronized List<ClientHandler> publishMessage(ClientHandler publisher, String topicName, String message) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.addMessage(message, publisher);
        }
        return topic.getSubscribers();
    }

    public synchronized Optional<List<Message>> listMessages(ClientHandler publisher, String topicName) {
        // Otteniamo il topic in modo sicuro con Optional
        return Optional.ofNullable(topics.get(topicName))
                       .map(topic -> topic.getMessagesByPublisher(publisher)); // Se esiste, otteniamo i messaggi
    }

    public synchronized Optional<List<Message>> listMessagesByTopic(String topicName) {
        // Otteniamo il topic in modo sicuro con Optional
        return Optional.ofNullable(topics.get(topicName))
                       .map(topic -> {
                           // Otteniamo la mappa di ClientHandler -> List<Message>
                           HashMap<ClientHandler, List<Message>> messagesMap = topic.getAllMessages();
                           
                           // Combiniamo tutte le liste di messaggi in una singola lista
                           return messagesMap.values().stream()
                                             .flatMap(List::stream)
                                             .toList();  // Ritorna una singola lista con tutti i messaggi
                       });
    }

    // Metodo per ottenere una lista delle chiavi (i nomi dei topic)
    public synchronized List<String> getTopicNames() {
        return new ArrayList<>(topics.keySet());
    }

    // public synchronized void listAllMessages(ClientHandler publisher, String topicName) {
    //     Topic topic = topics.get(topicName);
    //     if (topic != null) {
    //         topic.listAllMessages(publisher, topic);
    //     }
    // }
}
