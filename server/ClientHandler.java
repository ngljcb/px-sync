import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable{

    Socket socket;
    TopicManager resource;

    public ClientHandler(Socket socket, TopicManager resource) {
        this.socket = socket;
        this.resource = resource;
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(this.socket.getInputStream());
            PrintWriter toClient = new PrintWriter(this.socket.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " listening...");

            boolean closed = false;
            while (!closed) {
                String request = fromClient.nextLine();
                String[] parts = request.split(" ", 3);
                if (!Thread.interrupted()) {
                    System.out.println("Request: " + request);
                    try {
                        switch (parts[0]) {
                            case "quit":
                                closed = true;
                                toClient.println("quit");
                                break;
                            case "publish":
                                if (parts.length > 1) {
                                    String topic = parts[1];
                                    this.resource.addPublisher(this, topic);
                                } else {
                                    toClient.println("No key");
                                }
                                break;
                            case "subscribe":
                                if (parts.length > 1) {
                                    String topic = parts[1];
                                    this.resource.addSubscriber(this, topic);
                                } else {
                                    toClient.println("No key");
                                }
                                break;
                            case "list":
                                if (parts.length > 1) {
                                    String topic = parts[1];
                                    this.resource.listMessages(this, topic);
                                } else {
                                    toClient.println("Unknown command");
                                }
                                break;
                            case "listall":
                                if (parts.length > 1) {
                                    String topic = parts[1];
                                    this.resource.listAllMessages(this, topic);
                                } else {
                                    toClient.println("Unknown command");
                                }
                                break;
                            default:
                                toClient.println("Unknown command");
                        }
                    } catch (InterruptedException e) {
                        /*
                         * se riceviamo un Thread.interrupt() mentre siamo in attesa di add() o
                         * extract(), interrompiamo il ciclo come richiesto, e passiamo alla chiusura
                         * del socket
                         */
                        toClient.println("quit");
                        break;
                    }
                } else {
                    break;
                }
            }

            toClient.println("quit");

            toClient.close();
            fromClient.close();

            this.socket.close();
            System.out.println("Closed");

        } catch(IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
            e.printStackTrace();
        }
    }

}
