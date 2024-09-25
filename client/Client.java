import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            // Collegamento al server
            System.out.println("Connected to server");
            
            Scanner userInput = new Scanner(System.in);
            
            while (true) {
                Socket socket = new Socket(host, port);
                // Accettazione comandi da parte dell'utente
                System.out.println("Usage: publish <topic> / subscribe <topic> / show / quit");    
                String command = userInput.nextLine();
                
                if (command.startsWith("publish ") && command.split(" ").length > 1) {
                    Thread publisher = new Thread(new Publisher(socket, command.split(" ")[1]));
                    publisher.start();
                    try {
                        publisher.join();
                        break;
                    } catch (InterruptedException e) {
                        return;
                    }
                } else if (command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    Thread subscriber = new Thread(new Subscriber(socket, command.split(" ")[1]));
                    subscriber.start();
                    try {
                        subscriber.join();
                        break;
                    } catch (InterruptedException e) {
                        return;
                    }
                } else if (command.equals("show")) {
                    // Creiamo un CountDownLatch con conteggio 1
                    CountDownLatch latch = new CountDownLatch(1);

                    // Creiamo e avviamo il thread ShowTopics
                    Thread showtopics = new Thread(new ShowTopics(socket, latch));
                    showtopics.start();

                    try {
                        // Attendere che ShowTopics completi il suo lavoro
                        latch.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                    // Ora il flusso torna a Client
                } else if (command.equals("quit")) {
                    System.out.println("Disconnecting from server...");
                    break;
                } else {
                    System.out.println("Unknown command");
                }                
                socket.close();
            }

            userInput.close();
            System.out.println("Client: Socket closed.");

        } catch (IOException e) {
            System.err.println("ConnectException caught");
            e.printStackTrace();
        }
    }
}
