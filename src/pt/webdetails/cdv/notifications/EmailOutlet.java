/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.notifications;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.metadata.messages.LocaleHelper;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pdpi
 */
public class EmailOutlet implements NotificationOutlet {

    private static final Log logger = LogFactory.getLog(EmailOutlet.class);
    public static final String INPUT_TO = "to";
    public static final String INPUT_FROM = "from";
    public static final String INPUT_CC = "cc";
    public static final String INPUT_BCC = "bcc";
    public static final String INPUT_SUBJECT = "subject";
    public static final String INPUT_MESSAGEPLAIN = "message-plain";
    public static final String INPUT_MESSAGEHTML = "message-html";
    public static final String INPUT_MIMEMESSAGE = "mime-message";
    private static Map<String, String> defaults;
    private Map<String, String> settings;

    public static void setDefaults(Node settings) {
        defaults = getSettingsFromNode(settings);
    }

    private static Map<String, String> getSettingsFromNode(Node settings) {
        Map<String, String> out = new HashMap<String, String>();
        for (Node node : (List<Node>) settings.selectNodes(".//property")) {
            String key = node.selectSingleNode("./@name").getText();
            String val = node.selectSingleNode("./@value").getText();
            out.put(key, val);
        }
        return out;
    }

    public EmailOutlet(Node node) {
        settings = getSettingsFromNode(node);
    }

    private String getSetting(String name) {
        if (settings.containsKey(name)) {
            return settings.get(name);
        } else {
            return defaults.get(name);
        }
    }

    @Override
    public void publish(Alert not) {
        logger.info("Emailing");
        email(not);
    }

    private boolean email(Alert alert) {

        try {


            // Get the session object
            final Session session = buildSession();

            // Create the message
            final MimeMessage msg = new MimeMessage(session);

            // From, to, etc.
            applyMessageHeaders(msg, alert);

            // Get main message multipart
            final Multipart multipartBody = getMultipartBody(session, alert);

            // Process attachments
            //final Multipart mainMultiPart = processAttachments(multipartBody);
            //msg.setContent(mainMultiPart);
            msg.setContent(multipartBody);

            // Send it

            //msg.setHeader("X-Mailer", MAILER); //$NON-NLS-1$
            msg.setSentDate(new Date());

            Transport.send(msg);

            return true;

        } catch (SendFailedException e) {
            logger.error(e);
        } catch (AuthenticationFailedException e) {
            logger.error(e);
        } catch (MessagingException me) {
            logger.error(me);
        } catch (IOException e) {
            logger.error(e);
        }


        return false;
    }

    private Session buildSession() {

        final Properties props = new Properties();

        try {
            final Document configDocument = PentahoSystem.getSystemSettings().getSystemSettingsDocument(
                    "smtp-email/email_config.xml"); //$NON-NLS-1$
            final List<Node> properties = configDocument.selectNodes("/email-smtp/properties/*"); //$NON-NLS-1$
            for (Node propertyNode : properties) {
                final String propertyName = propertyNode.getName();
                final String propertyValue = propertyNode.getText();
                props.put(propertyName, propertyValue);
            }
        } catch (Exception e) {
            logger.error("Failed to build session: " + e.getMessage());
        }

        final boolean authenticate = "true".equals(props.getProperty("mail.smtp.auth")); //$NON-NLS-1$//$NON-NLS-2$

        // Get a Session object

        final Session session;
        if (authenticate) {
            final Authenticator authenticator = new EmailAuthenticator();
            session = Session.getInstance(props, authenticator);
        } else {
            session = Session.getInstance(props);
        }

        // if debugging is not set in the email config file, match the
        // component debug setting
        if (!props.containsKey("mail.debug")) { //$NON-NLS-1$
            session.setDebug(true);
        }

        return session;

    }

    private String getMessageBody(Alert alert) {
        return alert.getMessage();
    }

    private String getSubject(Alert alert) {
        return alert.getSummary(); 
    }
    private Multipart getMultipartBody(final Session session, final Alert alert) throws MessagingException, IOException {

        // if we have a mimeMessage, use it. Otherwise, build one with what we have
        // We can have both a messageHtml and messageText. Build according to it

        MimeMultipart parentMultipart = new MimeMultipart();
        MimeBodyPart htmlBodyPart = null, textBodyPart = null;

        final String content = getMessageBody(alert);

        textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(content, "text/plain; charset=" + LocaleHelper.getSystemEncoding());
        final MimeMultipart textMultipart = new MimeMultipart();
        textMultipart.addBodyPart(textBodyPart);

        parentMultipart = textMultipart;

        // We have both text and html? Encapsulate it in a multipart/alternative

        if (htmlBodyPart != null && textBodyPart != null) {

            final MimeMultipart alternative = new MimeMultipart("alternative");
            alternative.addBodyPart(textBodyPart);
            alternative.addBodyPart(htmlBodyPart);

            parentMultipart = alternative;

        }

        return parentMultipart;
    }

    private void applyMessageHeaders(final MimeMessage msg, Alert alert) throws MessagingException {
        String from = getSetting("from"),
                to = getSetting("to"),
                cc = getSetting("cc"),
                bcc = getSetting("bcc"),
                subject = getSubject(alert);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

        if ((cc != null) && (cc.trim().length() > 0)) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
        }
        if ((bcc != null) && (bcc.trim().length() > 0)) {
            msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
        }

        if (subject != null) {
            msg.setSubject(subject, LocaleHelper.getSystemEncoding());


        }

    }

    private static class EmailAuthenticator extends Authenticator {

        private EmailAuthenticator() {
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            final String user = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.userid", null); //$NON-NLS-1$ //$NON-NLS-2$
            final String password = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.password", null); //$NON-NLS-1$ //$NON-NLS-2$
            return new PasswordAuthentication(user, password);
        }
    }
}
