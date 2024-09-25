import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

public class ShowTopics implements Runnable {
    private Socket socket;
    private CountDownLatch latch;

    public ShowTopics(Socket socket, CountDownLatch latch) {
        this.socket = socket;
        this.latch = latch;  // Latch per sincronizzare con il thread Client
    }

    @Override
    public void run() {
        try {
            // Inoltra la richiesta "show" al server tramite PrintWriter
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
            toServer.println("show");

            // Attendere la risposta del server
            Scanner fromServer = new Scanner(this.socket.getInputStream());
            String response;

            System.out.println("Elenco dei topic disponibili: ");
            // Assicura che tutti i topic vengano stampati
            while (fromServer.hasNextLine()) {
                response = fromServer.nextLine();
                if(response.equals("SHOWTOPICbreak")) {
                    break;
                } else {
                    System.out.println(response);
                }
            }

            fromServer.close();
            toServer.close();

            // Segnala al thread principale che il lavoro è stato completato
            latch.countDown(); 

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }
}
