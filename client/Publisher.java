import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Publisher implements Runnable {
    private Socket socket;
    private String topic;

    public Publisher(Socket socket, String topic) {
        this.socket = socket;
        this.topic = topic; 
    }

    @Override
    public void run() {
        // Oggetto che accetta input da tastiera
        Scanner userInput = new Scanner(System.in);
        try {
            // PrintWriter per inviare dati al server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
            // Scanner per ricevere dati dal server
            Scanner fromServer = new Scanner(this.socket.getInputStream());

            // visto che il primo commando viene perso durante la creazione del thread, ricreiamolo e inviamolo al server
            toServer.println("publish " + this.topic);
            // Attendere la conferma dal server
            String response = fromServer.nextLine();
            System.out.println("Server response: " + response);

            boolean quiting = false;

            while (!quiting) {
                String request = userInput.nextLine();

                if (request.startsWith("send ")) {
                    // Invia messaggio al server
                    String message = request.substring(5); // Prendi il testo dopo "send "
                    toServer.println("send " + this.topic + " " + message);

                    // Attendere la conferma dal server
                    response = fromServer.nextLine();
                    System.out.println("Server response: " + response);

                } else if (request.equals("list")) {
                    // Invia richiesta per listare i messaggi del publisher
                    toServer.println("list " + this.topic);

                    // Attendere la risposta del server
                    response = fromServer.nextLine();
                    System.out.println("List of your messages: " + response);

                } else if (request.equals("listall")) {
                    // Invia richiesta per listare tutti i messaggi nel topic
                    toServer.println("listall " + this.topic);

                    // Attendere la risposta del server
                    response = fromServer.nextLine();
                    System.out.println("List of all messages: " + response);

                } else if (request.equals("quit")) {
                    // Invia richiesta per listare i messaggi del publisher
                    toServer.println("quit");
                    // toServer.close();
                    // System.out.println("Publisher closed");
                    quiting=true;

                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }
            
            // Chiudi le risorse
            fromServer.close();
            toServer.close();
            userInput.close();

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } catch(Exception e){
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione."); 
        }
    }
}
