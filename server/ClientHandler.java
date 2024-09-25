import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
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
                String requestLine = fromClient.nextLine();
                String[] parts = requestLine.split(" ", 3);
                String request = parts[0];
                String topic = parts.length > 1 ? parts[1] : "";
                String message = parts.length > 2 ? parts[2] : "";

                if (!Thread.interrupted()) {
                    System.out.println("ClientHandler: request type: "+request);
                    switch (request) {
                        case "quit":
                            closed = true;
                            System.out.println(this.toString() + " is quitting...");
                            break;
                        case "publish":
                            if (parts.length > 1) {                                
                                this.resource.addPublisher(this, topic);
                                toClient.println("publisher is added to topic "+topic);
                            } else {
                                toClient.println("No key");
                            }
                            break;
                        case "subscribe":
                            if (parts.length > 1) {
                                this.resource.addSubscriber(this, topic);
                                toClient.println("ClientHandler: added a subscriber to " + topic + "topic");
                            } else {
                                toClient.println("No key");
                            }
                            break;
                        case "list":
                            if (parts.length > 1) {
                                Optional<List<Message>> optionalMessages = this.resource.listMessages(this, topic);
                                optionalMessages.ifPresentOrElse(
                                    messages -> messages.forEach(toClient::println),                               // Stampa ogni messaggio
                                                () -> toClient.println("No messages found for the given topic.") // In caso di assenza di messaggi
                                );
                                toClient.println("ClientHandler: list messages reagrding the " + topic + "topic");
                            } else {
                                toClient.println("ClientHandler: Unknown command");
                            }
                            break;
                        case "listall":
                            if (parts.length > 1) {
                                Optional<List<Message>> optionalMessages = this.resource.listMessagesByTopic(topic);
                                optionalMessages.ifPresentOrElse(
                                    messages -> messages.forEach(toClient::println),                              // Stampa ogni messaggio
                                                () -> toClient.println("No messages found for the given topic.")  // In caso di assenza di messaggi
                                );
                                toClient.println("ClientHandler: list all messages reagrding the " + topic + "topic");
                            } else {
                                toClient.println("ClientHandler: Unknown command");
                            }
                            break;
                        case "send":
                            if (parts.length > 1) {
                                List<ClientHandler> subs = this.resource.publishMessage(this, topic, message);
                                
                                subs.forEach(s -> {
                                    try {
                                        PrintWriter sender = new PrintWriter(s.socket.getOutputStream(), true);
                                        sender.println(message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                toClient.println("ClientHandler: publisher sent a message to the " + topic + "topic");

                            } else {
                                toClient.println("ClientHandler: Unknown command");
                            }
                            break;
                        case "show":
                            List<String> topicNames = this.resource.getTopicNames();
                            topicNames.forEach(toClient::println);
                            toClient.println("SHOWTOPICbreak");
                            break;
                        default:
                            toClient.println("ClientHandler: Unknown command");
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
        } catch(Exception e){
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione."); 
        }
    }

}
