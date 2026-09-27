package com.meetbeat.matching.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Meet & Beat — Código de recuperação de senha");
        mensagem.setText("Seu código de recuperação de senha é: " + codigo
                + "\n\nEle é válido por 15 minutos. Se você não pediu essa recuperação, pode ignorar este e-mail.");

        mailSender.send(mensagem);
    }
}
