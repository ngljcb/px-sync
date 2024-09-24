import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Subscriber implements Runnable {
    private Socket socket;
    private String topic;
    private volatile boolean quiting = false;  // Variabile condivisa tra thread

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
            
            // visto che il primo commando viene perso durante la creazione del thread, ricreiamolo e inviamolo al server
            toServer.println("subscribe " + this.topic);
            
            // Crea un thread separato per ricevere i messaggi dal server
            Thread serverListener = new Thread(() -> {
                try {
                    // Scanner per ricevere dati dal server
                    Scanner fromServer = new Scanner(this.socket.getInputStream());

                    // Attendere la conferma dal server
                    String response = fromServer.nextLine();
                    System.out.println("Server response: " + response);

                    while (!quiting) {
                        // Leggi messaggi dal server
                        response = fromServer.nextLine();
                        System.out.println("Message from server: " + response);

                        if (response.equals("quit")) {
                            System.out.println("Server has disconnected.");
                            quiting = true;
                            break;
                        }
                    }
                    fromServer.close();
                } catch (Exception e) {
                    System.out.println("Connection to server lost.");
                }
            });

            // Avvia il thread per ascoltare i messaggi dal server
            serverListener.start();

            // Gestisci input dell'utente nel thread principale
            while (!quiting) {
                String request = userInput.nextLine();

                if (request.equals("listall")) {
                    // Invia richiesta per listare tutti i messaggi nel topic
                    toServer.println("list" + this.topic);


                } else if (request.equals("quit")) {
                    toServer.println("quit");
                    quiting = true;

                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }

            // Attendere la terminazione del thread serverListener
            serverListener.join();

            // Chiudi le risorse
            toServer.close();
            userInput.close();

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione.");
        }
    }
}
