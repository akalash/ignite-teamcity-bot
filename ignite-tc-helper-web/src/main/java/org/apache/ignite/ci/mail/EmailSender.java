/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ci.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import javax.mail.internet.MimeMultipart;
import org.apache.ignite.ci.HelperConfig;
import org.apache.ignite.ci.conf.PasswordEncoder;

/**
 * Created by Дмитрий on 21.05.2016
 */
public class EmailSender {
    public static void main(String[] args) {

        // Recipient's email ID needs to be mentioned.
        String to = "dpavlov.spb@gmail.com";

        String html = "<p>This is actual message</p>";

        String subject = "This is the Subject Line!";

        sendEmail(to, subject, html, "This is actual message.");
    }

    public static void sendEmail(String to, String subject, String html, String plainText) {
        Properties cfgProps = HelperConfig.loadEmailSettings();
        String from = HelperConfig.getMandatoryProperty(cfgProps, HelperConfig.USERNAME, HelperConfig. MAIL_PROPS);
        String enc = HelperConfig.getMandatoryProperty(cfgProps, HelperConfig.ENCODED_PASSWORD, HelperConfig.MAIL_PROPS);

        String pwd = PasswordEncoder.decode(enc);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",  "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // Sender's email ID needs to be mentioned
        // Setup mail getOrCreateCreds
        // Get the default Session object.

        Session ses = Session.getInstance(props,
                new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, pwd);
                    }
                });
        try {
            // Create a default MimeMessage object.
            MimeMessage msg = new MimeMessage(ses);

            // Set From: header field of the header.
            msg.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            msg.setSubject(subject);

            final MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(plainText, "text/plain");
            // HTML version
            final MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html");

            // Create the Multipart.  Add BodyParts to it.
            final Multipart mp = new MimeMultipart("alternative");
            mp.addBodyPart(textPart);
            mp.addBodyPart(htmlPart);
            // Set Multipart as the message's content
            msg.setContent(mp);

            // Send message
            Transport.send(msg);

            System.out.println("Sent message successfully to [" + to + "]...");
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
