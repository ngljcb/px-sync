import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private int id; // ID univoco del messaggio
    private String message; // Contenuto del messaggio
    private String dateTimestamp; // Data e ora in cui il messaggio è stato creato
    private ClientHandler publisher; // Riferimento al publisher che ha inviato il messaggio
    
    /**
     * Costruttore della classe Message.
     * 
     * @param id L'ID univoco del messaggio.
     * @param message Il contenuto del messaggio.
     * @param publisher Il ClientHandler che ha pubblicato il messaggio.
     */
    public Message(int id, String message, ClientHandler publisher) {
        this.id = id;
        this.message = message;
        this.publisher = publisher;
        this.dateTimestamp = generateTimestamp();  // Genera automaticamente il timestamp al momento della creazione del messaggio
    }
    
    /**
     * Genera un timestamp corrente nel formato desiderato e lo converte in stringa.
     * 
     * @return Il timestamp formattato come stringa.
     */
    private String generateTimestamp() {
        // Definisce il formato del timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        // Restituisce la data corrente formattata
        return sdf.format(new Date());
    }

    /**
     * Restituisce l'ID del messaggio.
     * 
     * @return L'ID univoco del messaggio.
     */
    public int getid() {
        return this.id;
    }

    /**
     * Metodo toString per visualizzare i dettagli del messaggio.
     * 
     * @return Una stringa formattata contenente l'ID, il contenuto, il timestamp e il publisher del messaggio.
     */
    @Override
    public String toString() {
        return "   - ID: " + this.id + "\n" +
               "     Message: " + this.message + "\n" +
               "     Date: " + this.dateTimestamp + "\n" +
               "     Publisher: " + this.publisher + "\n";
    }
}