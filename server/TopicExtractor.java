import java.util.List;

public class TopicExtractor implements Runnable {
   //attraverso questo oggetto possiamo accedere ai topic memorizzati
   private final TopicManager topicManager;

   public TopicExtractor(TopicManager topicManager) {
       this.topicManager = topicManager;
   }
   
    @Override   
    public void run() {
        try {
            //estrazione dei topic
            List<String> topics = topicManager.getTopics();

            //mostra topics
            if (topics.isEmpty()) {
                System.out.println("Nessun topic disponibile.");
            } else {
                System.out.println("Topics:");
                for (String topic : topics) {
                    System.out.println("- " + topic);
                }
            }
            } catch (Exception e) {
                System.out.println("Si e' verificato un errore durante l'estrazione dei topic: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    

}   