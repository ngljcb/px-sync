import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

public class Client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso corretto: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            System.out.println("Connesso al server");

            Scanner userInput = new Scanner(System.in);

            // Usa un array di dimensione 1 per tracciare lo stato di "quit"
            final boolean[] quit = {false};

            // Crea una connessione socket al server
            Socket socket = new Socket(host, port);

            // Variabili per tenere traccia dei thread attivi
            final Thread[] activeThread = {null};  // Tiene traccia del thread Publisher o Subscriber attivo

            // Thread separato per ascoltare i messaggi dal server
            Thread serverListener = new Thread(() -> {
                try {
                    Scanner fromServer = new Scanner(socket.getInputStream());

                    while (!Thread.currentThread().isInterrupted() && !quit[0] && fromServer.hasNextLine()) {
                        String response = fromServer.nextLine();

                        if (response.equals("quit")) {
                            System.out.println("\nErrore: Il server si è disconnesso.\n");
                            System.out.println("Comandi disponibili >> quit");

                            // Interrompe eventuali thread attivi
                            if (activeThread[0] != null && activeThread[0].isAlive()) {
                                System.out.println("sono qui");
                                activeThread[0].interrupt();
                            }

                            quit[0] = true;
                            break;
                        } else {
                            System.out.println("Messaggio dal server: " + response);
                        }
                    }

                    fromServer.close();
                } catch (IOException e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        System.out.println("Connessione al server persa.");
                    }
                }
            });

            serverListener.start();

            // Ciclo principale per gestire i comandi dell'utente
            while (!quit[0]) {
                // Aggiunta di una pausa di 2 secondi tra ogni iterazione
                Thread.sleep(2000);

                System.out.println("Comandi disponibili  >>  publish <topic> / subscribe <topic> / show / quit");
                String command = userInput.nextLine();

                // Se il comando è "publish"
                if (command.startsWith("publish ") && command.split(" ").length > 1) {
                    if (serverListener.isAlive()) {
                        serverListener.interrupt();
                    }

                    Socket publishSocket = new Socket(host, port);
                    Thread publisher = new Thread(new Publisher(publishSocket, command.split(" ")[1]));
                    activeThread[0] = publisher;
                    publisher.start();

                    try {
                        publisher.join();
                        quit[0] = true;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                // Se il comando è "subscribe"
                else if (command.startsWith("subscribe ") && command.split(" ").length > 1) {
                    if (serverListener.isAlive()) {
                        serverListener.interrupt();
                    }

                    Socket subscribeSocket = new Socket(host, port);
                    Thread subscriber = new Thread(new Subscriber(subscribeSocket, command.split(" ")[1]));
                    activeThread[0] = subscriber;
                    subscriber.start();

                    try {
                        subscriber.join();
                        quit[0] = true;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                // Se il comando è "show"
                else if (command.equals("show")) {
                    CountDownLatch latch = new CountDownLatch(1);

                    Socket showSocket = new Socket(host, port);
                    Thread showtopics = new Thread(new ShowTopics(showSocket, latch));
                    showtopics.start();

                    try {
                        showtopics.join();  // Attende la terminazione di showtopics
                        latch.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                // Se il comando è "quit"
                else if (command.equals("quit")) {
                    // Invia al server il comando "quit"
                    if (!socket.isClosed()) {
                        PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                        toServer.println("quit");
                        System.out.println("Disconnessione dal server...");
                    }

                    // Interrompe eventuali thread attivi
                    if (activeThread[0] != null && activeThread[0].isAlive()) {
                        activeThread[0].interrupt();
                        try {
                            activeThread[0].join();  // Attende che il thread attivo termini
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    quit[0] = true;
                }
                // Comando sconosciuto
                else {
                    System.out.println("Comando sconosciuto");
                }
            }

            // Chiude il socket
            socket.close();

            // Gestisce l'eccezione per il join del thread serverListener
            try {
                if (serverListener.isAlive()) {
                    serverListener.interrupt();
                    serverListener.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Chiude le risorse
            userInput.close();
            System.out.println("Client: Socket chiuso.");

        } catch (IOException e) {
            System.err.println("Errore di connessione rilevato");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrotto: " + e.getMessage());
        }
    }
}