import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {

    /**
     * Metodo principale che avvia il client e gestisce l'interazione con il server.
     * 
     * @param args Gli argomenti della riga di comando, dove args[0] rappresenta l'host
     *             e args[1] rappresenta la porta del server a cui connettersi.
     */
    public static void main(String[] args) {
        // Verifica se gli argomenti host e porta sono stati forniti
        if (args.length < 2) {
            System.err.println("Uso corretto: java Client <host> <port>");
            return;
        }

        // Ottiene l'host e la porta dai parametri della riga di comando
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            // Collegamento al server
            System.out.println("Connesso al server");

            // Scanner per accettare input da tastiera
            Scanner userInput = new Scanner(System.in);

            // Ciclo principale per gestire i comandi dell'utente
            while (true) {
                // Crea una connessione socket al server
                Socket socket = new Socket(host, port);

                // Mostra all'utente come usare il client
                System.out.println("Comandi disponibili  >>  publish <topic> / subscribe <topic> / show / quit");
                String command = userInput.nextLine();

                // Se il comando è "publish", avvia il thread Publisher
                if (command.startsWith("publish ") && command.split(" ").length > 1) {
                    Thread publisher = new Thread(new Publisher(socket, command.split(" ")[1]));
                    publisher.start();
                    try {
                        // Attendi che il thread Publisher termini
                        publisher.join();
                        break; // Esce dal ciclo dopo la pubblicazione
                    } catch (InterruptedException e) {
                        return;
                    }
                } 
                // Se il comando è "subscribe", avvia il thread Subscriber
                else if (command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    Thread subscriber = new Thread(new Subscriber(socket, command.split(" ")[1]));
                    subscriber.start();
                    try {
                        // Attendi che il thread Subscriber termini
                        subscriber.join();
                        break; // Esce dal ciclo dopo la sottoscrizione
                    } catch (InterruptedException e) {
                        return;
                    }
                } 
                // Se il comando è "show", avvia il thread ShowTopics
                else if (command.equals("show")) {
                    // Creiamo un CountDownLatch con conteggio 1 per sincronizzare i thread
                    CountDownLatch latch = new CountDownLatch(1);

                    // Creiamo e avviamo il thread ShowTopics per mostrare i topic
                    Thread showtopics = new Thread(new ShowTopics(socket, latch));
                    showtopics.start();

                    try {
                        // Attendere che ShowTopics completi il suo lavoro
                        latch.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                    // Ora il flusso torna a Client
                } 
                // Se il comando è "quit", interrompe la connessione
                else if (command.equals("quit")) {
                    System.out.println("Disconnessione dal server...");
                    break;
                } 
                // Se il comando non è valido, mostra un messaggio di errore
                else {
                    System.out.println("Comando sconosciuto");
                }

                // Chiude il socket dopo aver eseguito il comando
                socket.close();
            }

            // Chiude le risorse usate dall'utente
            userInput.close();
            System.out.println("Client: Socket chiuso.");

        } catch (IOException e) {
            // Gestione dell'errore di connessione al server
            System.err.println("Errore di connessione rilevato");
            e.printStackTrace();
        }
    }
}