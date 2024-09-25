import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class Topic {
    private String name; // Nome del topic
    private HashMap<ClientHandler, List<Message>> messages; // Lista dei messaggi
    private List<ClientHandler> subscribers; // Lista dei subscriber
    private List<ClientHandler> publishers; // Lista dei publishers
    private int counter; // Contatore per gli ID dei messaggi

    public Topic(String name) {
        this.name = name;
        this.messages = new HashMap<>(); // TODO: create hashmap<publisher, List<Messages>>
        this.subscribers = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.counter = 0;
    }

    public synchronized void addMessage(String text, ClientHandler publisher) {
        this.counter++;
        // Message message = new Message(this.counter, text, publisher);
        if(messages.containsKey(publisher)) {
            List<Message> msgs = messages.get(publisher);
            msgs.add(new Message(counter, text, publisher));
        } else {
            List<Message> msgs = new ArrayList<>();
            msgs.add(new Message(counter, text, publisher));
            messages.put(publisher, msgs);
        }
    }

    public synchronized void addSubscriber(ClientHandler subscriber) {
        subscribers.add(subscriber);
    }

    public synchronized void addPublisher(ClientHandler publisher) {
        publishers.add(publisher);
    }

    public synchronized List<Message> getMessagesByPublisher(ClientHandler publisher) {
        return this.messages.get(publisher);
    }

    public synchronized HashMap<ClientHandler, List<Message>> getAllMessages() {
        return this.messages;
    }

    public synchronized List<ClientHandler> getSubscribers() {
        return this.subscribers;
    }

    // public synchronized void deleteMessage(int id) {
    //     Iterator<Message> iterator = this.messages.iterator();
    //     while (iterator.hasNext()) {
    //         Message message = iterator.next();
    //         if (message.getid() == id) {
    //             iterator.remove(); // Rimuove il messaggio
    //             break;
    //         }
    //     }
    // }

    // private void notifySubscribers(Message message) {
    //     for (ClientHandler subscriber : this.subscribers) {
    //         subscriber.sendMessage(message); // Invia il messaggio ai subscriber
    //     }
    // }
}
