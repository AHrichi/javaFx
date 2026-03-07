package Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "votre.email@gmail.com"; // À configurer ou via env
    private final String password = "votre-app-password"; // Mot de passe d'application

    private Session createSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEmail(String to, String subject, String content) {
        // Exécution asynchrone pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                Message message = new MimeMessage(createSession());
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(content);

                Transport.send(message);
                System.out.println("Email envoyé avec succès à " + to);
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à " + to + ": " + e.getMessage());
            }
        }).start();
    }

    public void sendApprovalEmail(String toEmail, String entityName) {
        String subject = "Demande d'adhésion acceptée !";
        String content = "Félicitations !\n\nVotre demande pour rejoindre '" + entityName
                + "' a été approuvée par l'administrateur.\n\nSportivement,\nL'équipe SportLink";
        sendEmail(toEmail, subject, content);
    }

    public void sendRefusalEmail(String toEmail, String entityName) {
        String subject = "Mise à jour de votre demande d'adhésion";
        String content = "Bonjour,\n\nNous avons le regret de vous informer que votre demande pour rejoindre '"
                + entityName + "' n'a pas pu être retenue pour le moment.\n\nSportivement,\nL'équipe SportLink";
        sendEmail(toEmail, subject, content);
    }
}
