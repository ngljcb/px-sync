import java.util.HashMap;
import java.util.Scanner;

public class TopicManager {
    private HashMap<String, Topic> topics;

    public TopicManager() {
        this.topics = new HashMap<>();
    }

    public synchronized void addPublisher(ClientHandler handler, String topicName) {
        topics.computeIfAbsent(topicName, Topic::new);
        handler.sendMessage("Publisher registered for topic: " + topicName);
    }

    public synchronized void addSubscriber(ClientHandler handler, String topicName) {
        Topic topic = topics.computeIfAbsent(topicName, Topic::new);
        topic.addSubscriber(handler);
        handler.sendMessage("Subscriber registered for topic: " + topicName);
    }

    public synchronized void publishMessage(ClientHandler publisher, String topicName, String message) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.addMessage(message, publisher);
        }
    }

    public synchronized void listMessages(ClientHandler publisher, String topicName) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.listMessages(publisher);
        }
    }

    public synchronized void listAllMessages(ClientHandler publisher, String topicName) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.listAllMessages(publisher);
        }
    }
}
