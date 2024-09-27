import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;

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

            // Crea una connessione socket al server
            Socket socket = new Socket(host, port);

            // Crea un thread separato per ascoltare i messaggi dal server
            Thread serverListener = new Thread(() -> {
                try {
                    // Scanner per ricevere i dati dal server
                    Scanner fromServer = new Scanner(socket.getInputStream());

                    // Continua a leggere messaggi dal server finché il client non si disconnette
                    while (fromServer.hasNextLine()) {
                        String response = fromServer.nextLine();

                        // Se il server invia il comando "quit", interrompe il ciclo
                        if (response.equals("quit")) {
                            System.out.println("\nErrore: Il server si è disconnesso.\n");
                            System.out.println("Comandi disponibili >> quit");
                            break;
                        }
                    }

                    // Chiude il canale di comunicazione con il server
                    fromServer.close();
                } catch (IOException e) {
                    System.out.println("Connessione al server persa.");
                }
            });

            // Avvia il thread per ascoltare i messaggi dal server
            serverListener.start();

            // Ciclo principale per gestire i comandi dell'utente
            while (true) {
                if (!socket.isClosed()) {
                    // Mostra all'utente come usare il client
                    System.out.println("Comandi disponibili >> publish <topic> / subscribe <topic> / show / quit");
                } else {
                    // Mostra all'utente come usare il client
                    System.out.println("Comandi disponibili >> quit");
                }
                String command = userInput.nextLine();

                // Se il comando è "publish", avvia il thread Publisher
                if (!socket.isClosed() && command.startsWith("publish ") && command.split(" ").length > 1) {
                    Thread publisher = new Thread(new Publisher(socket, command.split(" ")[1]));
                    publisher.start();
                    try {
                        // Attendi che il thread Publisher termini
                        publisher.join();
                        break; // Esce dal ciclo dopo la pubblicazione
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                // Se il comando è "subscribe", avvia il thread Subscriber
                else if (!socket.isClosed() && command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    Thread subscriber = new Thread(new Subscriber(socket, command.split(" ")[1]));
                    subscriber.start();
                    try {
                        // Attendi che il thread Subscriber termini
                        subscriber.join();
                        break; // Esce dal ciclo dopo la sottoscrizione
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                // Se il comando è "show", invia una richiesta per mostrare i topic
                else if (!socket.isClosed() && command.equals("show")) {
                    PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                    toServer.println("show");
                }
                // Se il comando è "quit", interrompe la connessione
                else if (command.equals("quit")) {
                    if (!socket.isClosed()) {  // Controlla se il socket è ancora aperto
                        PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                        toServer.println("quit");
                        System.out.println("Disconnessione dal server...");
                    }
                    break;
                }
                // Se il comando non è valido, mostra un messaggio di errore
                else {
                    System.out.println("Comando sconosciuto. \n");
                }
            }

            // Chiude il socket dopo aver eseguito il comando
            socket.close();

            // Gestisce l'eccezione per il join del thread serverListener
            try {
                serverListener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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