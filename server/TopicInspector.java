import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TopicInspector implements Runnable {

    private Socket socket;

    public TopicInspector(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Crea PrintWriter e Scanner per comunicare con il server
            PrintWriter toServer = new PrintWriter(this.socket.getOutputStream(), true);
            Scanner fromServer = new Scanner(this.socket.getInputStream());

            // Invia la richiesta di ispezione al server
            toServer.println("inspect");

            // Ricevi e stampa la risposta dal server
            System.out.println("Inspecting topics:");
            while (fromServer.hasNextLine()) {
                String response = fromServer.nextLine();
                System.out.println(response);
            }

            // Chiudi le risorse
            fromServer.close();
            toServer.close();
            this.socket.close();

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }
}