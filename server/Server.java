import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Scanner userInput = new Scanner(System.in);

        try {
            // risorsa condivisa tra tutti i thread
            TopicManager topicManager = new TopicManager(); 

            ServerSocket server = new ServerSocket(port);
            /*
             * deleghiamo a un altro thread la gestione di tutte le connessioni; nel thread
             * principale ascoltiamo solo l'input da tastiera dell'utente (in caso voglia
             * chiudere il programma)
             */
            Thread serverThread = new Thread(new SocketListener(server, topicManager));
            serverThread.start();

            String command = "";

            while (!command.equals("quit")) {
                command = userInput.nextLine();

                // nel caso del commando "show", delega la gestione di IO ad un thread separato estrarre tutti i topic
                if (command.startsWith("show")) {
                    Thread topicExtractor = new Thread(new TopicExtractor());
                    topicExtractor.start();
                    try {
                        topicExtractor.join();
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                // nel caso del commando "inspect", delega la gestione di IO ad un thread separato per il Subscriber
                } else if (command.startsWith("inspect")) {
                    Thread topicInspector = new Thread(new TopicInspector());
                    topicInspector.start();
                    try {
                        topicInspector.join();
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                } else {
                    System.out.println("Unknown command");
                }
            }

            try {
                serverThread.interrupt();
                /* attendi la terminazione del thread */
                serverThread.join();
            } catch (InterruptedException e) {
                /*
                 * se qualcuno interrompe questo thread nel frattempo, terminiamo
                 */
                return;
            }
            System.out.println("Main thread terminated.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }
}
