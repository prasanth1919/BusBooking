package com.byft;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    /**
     * Sends an email using the provided credentials and email details.
     *
     * @param username  The email address to send from (e.g., your Gmail address).
     * @param password  The email password or app password for the sending account.
     * @param toEmail   The recipient's email address.
     * @param subject   The subject of the email.
     * @param body      The body content of the email.
     */
    public static void sendEmail(final String username, final String password, String toEmail, String subject, String body) {
        // Set SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP server address
        props.put("mail.smtp.port", "587"); // SMTP port for TLS

        // Create a mail session with authentication
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // Sender's email address
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // Recipient's email address
            message.setSubject(subject); // Email subject
            message.setText(body); // Email body

            // Send the email
            Transport.send(message);

            // Log success message
            System.out.println("Email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            // Log error details
            System.err.println("Failed to send email to " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }
}
