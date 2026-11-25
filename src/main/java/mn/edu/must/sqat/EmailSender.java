package mn.edu.must.sqat;

public interface EmailSender {

    void sendEmail(String to, String subject, String body);
}
