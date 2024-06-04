


package com.hospital.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String emailEnvio,String cuerpo,String Asunto){
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom("citasjamud@gmail.com");
        mensaje.setTo(emailEnvio);
        mensaje.setText(cuerpo);
        mensaje.setSubject(Asunto);

        mailSender.send(mensaje);
    }
}