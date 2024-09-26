import java.util.List;

public class TopicExtractor implements Runnable {
    private TopicManager topicManager; // Risorsa condivisa

    // Costruttore che accetta la risorsa condivisa TopicManager
    public TopicExtractor(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    @Override
    public void run() {
        // Chiama la funzione getTopicNames() e stampa i risultati
        List<String> topicNames = topicManager.getTopicNames();

        // Stampa tutti i topic
        System.out.println("List of available topics:");
        topicNames.forEach(System.out::println);
    }
}
