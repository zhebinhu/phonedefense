package com.example.huzb.phonedefense.Activity;

/**
 * Created by 34494 on 2017/1/30.
 */
import android.util.Log;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtils extends Authenticator {
    private String host;
    private String port;
    private String user;
    private String pass;
    private String from;
    private String to;
    private String subject;
    private String body;

    private Multipart multipart;
    private Properties props;

    public MailUtils() {
    }

    public MailUtils(String user, String pass, String from, String to, String host, String port,
                     String subject, String body) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public MailUtils setHost(String host) {
        this.host = host;
        return this;
    }

    public MailUtils setPort(String port) {
        this.port = port;
        return this;
    }

    public MailUtils setUser(String user) {
        this.user = user;
        return this;
    }

    public MailUtils setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public MailUtils setFrom(String from) {
        this.from = from;
        return this;
    }

    public MailUtils setTo(String to) {
        this.to = to;
        return this;
    }

    public MailUtils setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public MailUtils setBody(String body) {
        this.body = body;
        return this;
    }

    public void init() {
        multipart = new MimeMultipart();
        // There is something wrong with MailCap, javamail can not find a
        // handler for the multipart/mixed part, so this bit needs to be added.

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
    }

    public boolean send() throws MessagingException {
        if (!user.equals("") && !pass.equals("") && !to.equals("") && !from.equals("")) {
            Session session = Session.getDefaultInstance(props, this);
            Log.d("SendUtil", host + "..." + port + ".." + user + "..." );

            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            InternetAddress addressTo = new InternetAddress(to);
            msg.setRecipient(Message.RecipientType.TO, addressTo);

            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // setup message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            msg.setContent(multipart);

            // send email
            Transport.send(msg);

            return true;
        } else {
            return false;
        }
    }
    /**
     *  添加附件的方法（附件路径，附件文件名）
     */
    public void addAttachment(String filePath, String fileName) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        javax.activation.DataSource source = new FileDataSource(filePath+"/"+fileName);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
    }
}
