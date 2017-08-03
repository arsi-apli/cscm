/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.server.mail;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import sk.arsi.nb.help.server.config.ConfigManager;

/**
 *
 * @author arsi
 */
public class MailTools {

    private static final Logger LOG = Logger.getLogger(MailTools.class.getName());

    public static void sendEmail(String email, String subject, String message) throws AddressException, MessagingException {
        // Step1
        if (((boolean) ConfigManager.SMTP.get(ConfigManager.SMTP_ENABLE, Boolean.class))) {
            Properties mailServerProperties = System.getProperties();
            mailServerProperties.put("mail.smtp.password", ConfigManager.SMTP.get(ConfigManager.SMTP_PASSWORD, ""));
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail-smtps-user", ConfigManager.SMTP.get(ConfigManager.SMTP_USER, ""));
            mailServerProperties.put("mail.smtp.host", ConfigManager.SMTP.get(ConfigManager.SMTP_SERVER, ""));
            mailServerProperties.put("mail.smtp.ssl.enable", "true");
            mailServerProperties.put("mail.smtp.ssl.trust", "*");
            mailServerProperties.put("mail.smtp.ssl.checkserveridentity", "false");

            // Step2
            Session getMailSession = Session.getDefaultInstance(mailServerProperties, null);
            MimeMessage generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            generateMailMessage.setFrom(new InternetAddress(ConfigManager.SMTP.get(ConfigManager.SMTP_FROM, "")));
            generateMailMessage.setSubject(subject);
            generateMailMessage.setContent(message, "text/plain");

            // Step3
            Transport transport = getMailSession.getTransport("smtp");

            // Enter your correct gmail UserID and Password
            // if you have 2FA enabled then provide App Specific Password
            try {
                transport.connect(ConfigManager.SMTP.get(ConfigManager.SMTP_SERVER, ""), ConfigManager.SMTP.get(ConfigManager.SMTP_USER, ""), ConfigManager.SMTP.get(ConfigManager.SMTP_PASSWORD, ""));
                transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
                transport.close();
            } catch (Exception exception) {
                LOG.log(Level.SEVERE, "Mail send exception", exception);
            }
        }
    }
}
