import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TopicInspector implements Runnable {

    private TopicManager topicManager;  // Risorsa condivisa per gestire i topic
    private String topicName;

    /**
     * Costruttore della classe TopicInspector.
     * 
     * @param topicManager La risorsa condivisa TopicManager che gestisce i topic.
     * @param topicName Topic da ispezionare.
     */
    public TopicInspector(TopicManager topicManager, String topicName) {
        this.topicManager = topicManager;
        this.topicName = topicName;
    }

    /**
     * Metodo eseguito nel thread per avviare una sessione interattiva che permette
     * di analizzare e modificare i messaggi di un topic.
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        Optional<Topic> optionalTopic = topicManager.getTopicByName(topicName);

        Topic topic = optionalTopic.get();
        boolean interactiveSession = true;

        while (interactiveSession) {
            System.out.println("Comandi disponibili  >>  :listall / :delete <id> / :end");
            String command = scanner.nextLine();

            if (command.startsWith(":listall")) {
                List<Message> messages = topic.getAllMessagesAsList();
                if (messages.isEmpty()) {
                    System.out.println("\nSono stati inviati 0 messaggi per il topic " + topicName + ".");
                } else {
                    System.out.println("\nSono stati inviati " + messages.size() + " messaggi per il topic " + topicName + ".");
                    messages.forEach(System.out::println);
                }
            } else if (command.startsWith(":delete")) {
                String[] parts = command.split(" ");
                if (parts.length < 2) {
                    System.out.println("Inserisci un ID messaggio valido.");
                    continue;
                }

                try {
                    int messageId = Integer.parseInt(parts[1]);
                    boolean deleted = topic.deleteMessageById(messageId);
                    if (deleted) {
                        System.out.println("\nMessaggio con ID " + messageId + " eliminato.");
                    } else {
                        System.out.println("\nNessun messaggio con ID " + messageId + " trovato.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\nFormato ID non valido. Inserisci un numero valido.");
                }
            } else if (command.equals(":end")) {
                System.out.println("\nTerminazione della sessione...");
                interactiveSession = false;
            } else {
                System.out.println("Comando sconosciuto.");
            }
        }
    }
}