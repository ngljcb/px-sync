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
        // Oggetto che accetta input da tastiera
        Scanner userInput = new Scanner(System.in);
        try {
            // PrintWriter per inviare dati al server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
            // Scanner per ricevere dati dal server
            Scanner fromServer = new Scanner(this.socket.getInputStream());

            // visto che il primo commando viene perso durante la creazione del thread, ricreiamolo e inviamolo al server
            toServer.println("subscribe " + this.topic);
            // Attendere la conferma dal server
            String response = fromServer.nextLine();
            System.out.println("Server response: " + response);

            System.out.println("Actively listening... ");

            // Crea un thread separato per ricevere i messaggi dal server
            Thread serverListener = new Thread(() -> {
                try {
                    while (true) {
                        // Legge messaggi dal server
                        String serverMessage = fromServer.nextLine();
                        System.out.println("Message from server: " + serverMessage);
                        
                        if (serverMessage.equals("quit")) {
                            System.out.println("Server has disconnected.");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Connection to server lost.");
                }
            });

            // Avvia il thread per ascoltare i messaggi dal server
            serverListener.start();

            // Gestisce input dell'utente nel thread principale
            while (true) {
                String request = userInput.nextLine();

                if (request.equals("listall")) {
                    // Invia richiesta per listare tutti i messaggi nel topic
                    toServer.println("listall " + this.topic);

                } else if (request.equals("quit")) {
                    toServer.println("quit");
                    break;

                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }

            // Attendere che il thread di ascolto dei messaggi dal server termini
            serverListener.join();
            
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
