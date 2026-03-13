package com.wexec.zinde_server.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    @Value("${mail.from}")
    private String fromEmail;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendOtpEmail(String to, String code) {
        String body = "Merhaba,\n\n"
                + "Zinde hesabinizi dogrulamak icin asagidaki kodu kullanin:\n\n"
                + code + "\n\n"
                + "Bu kod 3 dakika gecerlidir.\n\n"
                + "Eger bu istegi siz yapmadiysaniz bu e-postayi gormezden gelebilirsiniz.\n\n"
                + "Zinde Ekibi";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject("Zinde - E-posta Dogrulama Kodunuz")
                .text(body)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Mail gönderilemedi: " + e.getMessage(), e);
        }
    }
}
