package com.hospital.utils;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service

public class EmailUtils {
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text, List<String> list){
        SimpleMailMessage message =new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if(list!= null && list.size() > 0){
            message.setCc(getCcArray(list));
            emailSender.send(message);
        }
       
    }

    private String[] getCcArray(List<String> ccList){
        String[] cc = new String[ccList.size()];

        for (int i = 0; i < cc.length; i++) {
            cc[i]=ccList.get(i);
        }
        return cc;
    }

    public void forgotMail(String to, String subject, String password) throws MessagingException{
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlsMsg = "<p><b> Your Login details for Hopsital</b><br><b>Email: </b>" + to + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">click here to login </a></p>";
        message.setContent(htmlsMsg, "text/html");
        emailSender.send(message);
    }
}
