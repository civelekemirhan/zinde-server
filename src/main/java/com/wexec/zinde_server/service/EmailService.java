package com.wexec.zinde_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String code) {
        log.info("OTP maili gönderiliyor → {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Zinde - E-posta Dogrulama Kodunuz");
            message.setText(
                    "Merhaba,\n\n"
                    + "Zinde hesabinizi dogrulamak icin asagidaki kodu kullanin:\n\n"
                    + code + "\n\n"
                    + "Bu kod 3 dakika gecerlidir.\n\n"
                    + "Eger bu istegi siz yapmadiysaniz bu e-postayi gormezden gelebilirsiniz.\n\n"
                    + "Zinde Ekibi"
            );
            mailSender.send(message);
            log.info("OTP maili başarıyla gönderildi → {}", to);
        } catch (Exception e) {
            log.error("OTP maili gönderilemedi → {} | hata: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}
