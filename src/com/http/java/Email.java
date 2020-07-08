package com.http.java;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class Email {

    public static void sendEmail(String toEmail, String subject, String body) {
        org.simplejavamail.api.email.Email email = EmailBuilder.startingBlank()
                .from("Wale Adekoya", "adekoya.wale@yahoo.com")
                .to("mom", toEmail)
                .withSubject(subject)
                .withPlainText(body)
                .buildEmail();

        Mailer mailer = MailerBuilder.withSMTPServer("smtp.mail.yahoo.com",
                587, "adekoya.wale@yahoo.com", "mwxxplzgxzbraibv")
                .buildMailer();
//        mailer.testConnection();
        mailer.sendMail(email);

        // https://www.bt.com/help/email/manage-email-account/manual-settings/what-are-the-settings-for-outgoing-and-incoming-bt-email-servers
        // https://www.codeproject.com/Articles/5266074/Send-Email-with-Java-and-Yahoo-Mail
        // http://www.simplejavamail.org/index.html#navigation

    }

    public static void main(String[] args) {
        sendEmail("wale.adekoya@btinternet.com", "Test Java Email",
                "\n Confirm if settings for BT Mail works as expected. \n BR\n Wale");
    }

}
