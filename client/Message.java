import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private int id;
    private String message;
    private String dateTimestamp;
    private ClientHandler publisher;
    
    public Message(int id, String message, ClientHandler publisher) {
        this.id = id;
        this.message = message;
        this.publisher = publisher;
        this.dateTimestamp = generateTimestamp();  // genera automaticamente il timestamp al momento della creazione del messaggio
    }
    
    // genera timestamp nel formato desiderato e converte in stringa
    private String generateTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date());
    }

    // getter ID
    public int getid() {
        return this.id;
    }

    // toString method x visualizzare dettagli messaggio
    @Override
    public String toString() {
    return "ID: " + this.id + "\n" +
           "Message: " + this.message + "\n" +
           "Date: " + this.dateTimestamp + "\n" +
           "Publisher: " + this.publisher;
    }
}