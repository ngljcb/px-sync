import java.util.List;

public class TopicExtractor implements Runnable {
    
    // Risorsa condivisa per gestire i topic
    private TopicManager topicManager;

    /**
     * Costruttore della classe TopicExtractor.
     * 
     * @param topicManager La risorsa condivisa TopicManager che gestisce i topic.
     */
    public TopicExtractor(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    /**
     * Metodo eseguito nel thread per estrarre e stampare i nomi dei topic gestiti dal TopicManager.
     */
    @Override
    public void run() {
        // Chiama la funzione getTopicNames() per ottenere la lista dei topic
        List<String> topicNames = topicManager.getTopicNames();

        // Stampa tutti i topic ottenuti
        System.out.println("Topics:");
        topicNames.forEach(topic -> System.out.println(" - " + topic)); // Stampa ogni topic preceduto da un trattino
    }
}
