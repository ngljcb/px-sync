import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Subscriber implements Runnable {
    private Socket socket;
    private String topic;

    public Subscriber(Socket socket, String topic) {
        this.socket = socket;
        this.topic = topic; 
    }

    @Override
    public void run() {
        // oggetto che accetta input da tastiera
        Scanner userInput = new Scanner(System.in); 
        try {
            while (true) {
                String request = userInput.nextLine();
                if (request.equals("listall")) {
                    // inviare richiesta listall al server tramite printwriter
                    PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
                    toServer.println("listall " + this.topic);
                    
                    // attendere la risposta del server
                    Scanner fromServer = new Scanner(this.socket.getInputStream());
                    String response = fromServer.nextLine();
                    System.out.println("Received: " + response);

                    fromServer.close();
                    toServer.close();

                } else if (request.equals("quit")) {
                    System.out.println("Subscriber closed");
                    break;
                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }
            userInput.close();

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }
}