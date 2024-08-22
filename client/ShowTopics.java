import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class ShowTopics implements Runnable {
    private Socket socket;

    public ShowTopics (Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
                // inoltra la richiesta show al server tramite printwriter
                PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
                toServer.println("show ");
                    
                // attendere la risposta del server
                Scanner fromServer = new Scanner(this.socket.getInputStream());
                String response;

                System.out.println("Elenco dei topic disponibili: ");
                // assicura che tutti i topic vengano stampati
                while (fromServer.hasNextLine()) {
                    response = fromServer.nextLine();
                    System.out.println(response);
                }

                fromServer.close();
                toServer.close();
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }
}