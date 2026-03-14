package com.wexec.zinde_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final WebClient webClient;

    @Value("${mail.from}")
    private String fromEmail;

    public EmailService(@Value("${brevo.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendOtpEmail(String to, String code) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", "Zinde - E-posta Dogrulama Kodunuz",
                "textContent",
                "Merhaba,\n\n"
                + "Zinde hesabinizi dogrulamak icin asagidaki kodu kullanin:\n\n"
                + code + "\n\n"
                + "Bu kod 3 dakika gecerlidir.\n\n"
                + "Eger bu istegi siz yapmadiysaniz bu e-postayi gormezden gelebilirsiniz.\n\n"
                + "Zinde Ekibi"
        );

        webClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
