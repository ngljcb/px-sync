import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Scanner userInput = new Scanner(System.in);

        try {
            // collegamento al server
            Socket socket = new Socket(host, port);
            System.out.println("Connected to server");

            // accettazione commandi da parte dell'utente
            System.out.println("Usage: publish <topic> / subscribe <topic> / show / quit");
            String command = userInput.nextLine();

            while (true) {
                // nel caso del commando "publish", delega la gestione di IO ad un thread separato per il Publisher
                if (command.startsWith("publish ") && command.split(" ").length > 1) {
                    Thread publisher = new Thread(new Publisher(socket, command.split(" ")[1]));
                    publisher.start();
                    try {
                        publisher.join();
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                // nel caso del commando "subscribe", delega la gestione di IO ad un thread separato per il Subscriber
                } else if (command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    Thread subscriber = new Thread(new Subscriber(socket, command.split(" ")[1]));
                    subscriber.start();
                    try {
                        subscriber.join();
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                // nel caso del commando "show", invia la richiesta al server e attende del risultato
                } else if (command.equals("show")) {
                    Thread showtopics = new Thread(new ShowTopics(socket));
                    showtopics.start();
                    try {
                        showtopics.join();
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                } else if (command.equals("quit")) {
                    System.out.println("Disconnecting from server...");
                    break;
                } else {
                    System.out.println("Unknown command");
                }
            }

            socket.close();
            System.out.println("Socket closed.");

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }
}