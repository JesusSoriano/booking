/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Jesús Soriano
 */
@Stateless
public class MailService {

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String SMTP_HOST_PORT = "587";
    private static final String SMTP_USER = "jesus.sorianocandon@gmail.com";

    public MailService() {
    }

    public void sendRegistrationMail(User user, String newPassword, Organisation organisation) throws Exception {

        String subject = "Nuevo registro en " + organisation.getName();

        String content = "Hola " + user.getFirstName() + ".\n\n"
                + "Gracias por registrarte en " + organisation.getName()
                + ", uno de nuestros administradores aceptará tu solicitud de registro lo antes posible.\n\n"
                + "Si en 2 días laborables no has recibido el correo de confirmación de alta, por favor contacta con nosotros respondiendo a este email.\n\n"
                + organisation.getName();

        sendEmail(subject, content, user.getEmail(), organisation);
    }

    public void sendForgotPasswordMail(User user, String newPassword, Organisation organisation) throws Exception {

        String subject = "Recuperar contraseña";
        String content = "Hola " + user.getFirstName() + ".\n\n"
                + "Tu nueva contraseña es: " + newPassword + "\n\n"
                + "Por favor, entra en el sistema y establece una nueva contraseña personal.\n\n"
                + organisation.getName();

        sendEmail(subject, content, user.getEmail(), organisation);
    }

    private void sendEmail(final String subject, final String content, final String address, Organisation organisation) throws MessagingException {
        //mailProducer.sendEmail(subject, content, addresess, getMailSettings(organisation), false);

        Properties props = new Properties();

// Nombre del host de correo, es smtp.gmail.com
        props.setProperty("mail.smtp.host", SMTP_HOST_NAME);

// TLS si está disponible
        props.setProperty("mail.smtp.starttls.enable", "true");

// Puerto de gmail para envio de correos
        props.setProperty("mail.smtp.port", SMTP_HOST_PORT);

// Nombre del usuario
        props.setProperty("mail.smtp.user", SMTP_USER);

// Si requiere o no usuario y password para conectarse.
        props.setProperty("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);

        MimeMessage message = new MimeMessage(session);

        try {
            // Quien envia el correo
            message.setFrom(new InternetAddress(organisation.getEmail()));
        } catch (AddressException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        }

// A quien va dirigido
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));

        message.setSubject(subject);
        message.setText(content);
        
        // Send email
/*        Transport t = session.getTransport("smtp");
        t.connect("ejemplo@gmail.com","password");
        t.sendMessage(message,message.getAllRecipients());
        t.close();
*/
    }

    public void sendTestEmail(String to, Organisation organisation) {
        try {
            sendEmail("Test Email", "Email de prueba", to, organisation);
        } catch (MessagingException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
