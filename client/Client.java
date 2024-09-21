import java.io.IOException;
import java.net.Socket;
// import java.net.ConnectException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);


        try {
            // collegamento al server
            Socket socket = new Socket(host, port);
            System.out.println("Connected to server");

            // accettazione commandi da parte dell'utente
            System.out.println("Usage: publish <topic> / subscribe <topic> / show / quit");

            Scanner userInput = new Scanner(System.in);
            String command = userInput.nextLine();

            while (true) {
                // nel caso del commando "publish", delega la gestione di IO ad un thread separato per il Publisher
                if (command.startsWith("publish ") && command.split(" ").length > 1) {
                    Thread publisher = new Thread(new Publisher(socket, command.split(" ")[1]));
                    publisher.start();
                    try {
                        publisher.join();
                        break;
                    } catch (InterruptedException e) {
                        //se qualcuno interrompe questo thread nel frattempo, terminiamo
                        return;
                    }
                } else if (command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    Thread subscriber = new Thread(new Subscriber(socket, command.split(" ")[1]));
                    subscriber.start();
                    try {
                        subscriber.join();
                        break;
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

            userInput.close();
            socket.close();
            System.out.println("Client: Socket closed.");                

        } catch (IOException e) {
            System.err.println("ConnectException caught");
            e.printStackTrace();
        }
    }
}