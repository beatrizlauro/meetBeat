package com.meetbeat.matching.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    private static final String ASSUNTO = "Meet & Beat — Código de recuperação de senha";
    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

    private final JavaMailSender mailSender;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${spring.mail.username:}")
    private String remetente;

    // Preenchida apenas na nuvem: lá as portas SMTP são bloqueadas e o envio passa pela API HTTPS da Brevo.
    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        String texto = "Seu código de recuperação de senha é: " + codigo
                + "\n\nEle é válido por 15 minutos. Se você não pediu essa recuperação, pode ignorar este e-mail.";

        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            enviarPelaBrevo(destinatario, texto);
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(ASSUNTO);
        mensagem.setText(texto);
        mailSender.send(mensagem);
    }

    private void enviarPelaBrevo(String destinatario, String texto) {
        String corpo = "{\"sender\":{\"name\":\"Meet & Beat\",\"email\":" + json(remetente) + "},"
                + "\"to\":[{\"email\":" + json(destinatario) + "}],"
                + "\"subject\":" + json(ASSUNTO) + ","
                + "\"textContent\":" + json(texto) + "}";

        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(URL_BREVO))
                .timeout(Duration.ofSeconds(20))
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        try {
            HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() / 100 != 2) {
                throw new IllegalStateException("Brevo recusou o envio (" + resposta.statusCode() + "): " + resposta.body());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao enviar e-mail pela Brevo", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envio de e-mail interrompido", e);
        }
    }

    private static String json(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (valor == null ? "" : valor).toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
