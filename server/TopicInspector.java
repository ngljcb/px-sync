import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TopicInspector implements Runnable {

    // Risorsa condivisa per gestire i topic
    private TopicManager topicManager;

    /**
     * Costruttore della classe TopicInspector.
     * 
     * @param topicManager La risorsa condivisa TopicManager che gestisce i topic.
     */
    public TopicInspector(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    /**
     * Metodo eseguito nel thread per avviare una sessione interattiva che permette
     * di analizzare e modificare i messaggi di un topic.
     */
    @Override
    public void run() {
        // Scanner per l'input dell'utente
        Scanner scanner = new Scanner(System.in);

        // Chiede all'utente di inserire il nome del topic da ispezionare
        System.out.println("Inserisci il topic da ispezionare:");
        String topicName = scanner.nextLine();

        // Ottiene il topic dal TopicManager
        Optional<Topic> optionalTopic = topicManager.getTopicByName(topicName);

        // Controlla se il topic esiste
        if (optionalTopic.isEmpty()) {
            System.out.println("Errore: Topic non trovato. \n");
            return;
        }

        Topic topic = optionalTopic.get(); // Ottiene il topic
        boolean interactiveSession = true; // Variabile per controllare la sessione interattiva

        // Sessione interattiva per l'analisi del topic
        while (interactiveSession) {
            System.out.println("Comandi disponibili  >>  :listall / :delete <id> / :end");
            String command = scanner.nextLine();

            // Comando :listall per elencare tutti i messaggi nel topic
            if (command.startsWith(":listall")) {
                List<Message> messages = topic.getAllMessagesAsList();
                if (messages.isEmpty()) {
                    System.out.println("Sono stati inviati 0 messaggi per il topic " + topicName + ".");
                } else {
                    // Stampa il numero di messaggi
                    System.out.println("Sono stati inviati " + messages.size() + " messaggi per il topic " + topicName + ".");
                    // Stampa ogni messaggio
                    messages.forEach(System.out::println);
                }

                // Comando :delete per eliminare un messaggio con un determinato ID
            } else if (command.startsWith(":delete")) {
                String[] parts = command.split(" ");
                if (parts.length < 2) {
                    System.out.println("Inserisci un ID messaggio valido.");
                    continue;
                }

                // Prova a convertire l'ID in un numero intero e cancellare il messaggio
                try {
                    int messageId = Integer.parseInt(parts[1]);
                    boolean deleted = topic.deleteMessageById(messageId);
                    if (deleted) {
                        System.out.println("Messaggio con ID " + messageId + " eliminato.");
                    } else {
                        System.out.println("Nessun messaggio con ID " + messageId + " trovato.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Formato ID non valido. Inserisci un numero valido.");
                }

                // Comando :end per terminare la sessione interattiva
            } else if (command.equals(":end")) {
                System.out.println("Terminazione della sessione...");
                interactiveSession = false; // Termina la sessione
            } else {
                // Comando non riconosciuto
                System.out.println("Comando sconosciuto.");
            }
        }

        // Chiusura delle risorse
        scanner.close();
    }
}