import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TopicInspector implements Runnable {
    private TopicManager topicManager; // Risorsa condivisa

    // Costruttore che accetta la risorsa condivisa TopicManager
    public TopicInspector(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        // Chiede all'utente di inserire il nome del topic
        System.out.println("Enter the topic you want to inspect:");
        String topicName = scanner.nextLine();

        // Ottieni il topic dal TopicManager
        Optional<Topic> optionalTopic = topicManager.getTopicByName(topicName);

        if (optionalTopic.isEmpty()) {
            System.out.println("Topic not found.");
            return;
        }

        Topic topic = optionalTopic.get();
        boolean interactiveSession = true;

        // Sessione interattiva per l'analisi del topic
        while (interactiveSession) {
            System.out.println("Enter a command (:listall, :delete <id>, :end):");
            String command = scanner.nextLine();
            
            if (command.startsWith(":listall")) {
                // Comando per elencare tutti i messaggi nel topic
                System.out.println("Listing all messages in the topic:");
                List<Message> messages = topic.getAllMessagesAsList();
                if (messages.isEmpty()) {
                    System.out.println("No messages in this topic.");
                } else {
                    messages.forEach(System.out::println);
                }
            } else if (command.startsWith(":delete")) {
                // Comando per eliminare un messaggio
                String[] parts = command.split(" ");
                if (parts.length < 2) {
                    System.out.println("Please provide a valid message ID.");
                    continue;
                }

                try {
                    int messageId = Integer.parseInt(parts[1]);
                    boolean deleted = topic.deleteMessageById(messageId);
                    if (deleted) {
                        System.out.println("Message with ID " + messageId + " deleted.");
                    } else {
                        System.out.println("No message with ID " + messageId + " found.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format. Please enter a valid number.");
                }
            } else if (command.equals(":end")) {
                // Comando per terminare la sessione
                System.out.println("Ending the session...");
                interactiveSession = false;
            } else {
                System.out.println("Unknown command.");
            }
        }

        // Chiusura risorse
        scanner.close();
    }
}
