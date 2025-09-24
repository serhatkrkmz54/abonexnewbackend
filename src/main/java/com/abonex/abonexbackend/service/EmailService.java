package com.abonex.abonexbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Abonex Hesap Aktifleştirme Kodu");
            message.setText("Merhaba,\n\nAbonex hesabınızı yeniden aktifleştirmek için doğrulama kodunuz aşağıdadır:\n\n" +
                    "Kod: " + code + "\n\n" +
                    "Bu kod 10 dakika boyunca geçerlidir.\n\n" +
                    "Teşekkürler,\nAbonex Ekibi");
            mailSender.send(message);
            log.info("Doğrulama kodu başarıyla gönderildi: {}", toEmail);
        } catch (Exception e) {
            log.error("E-posta gönderilirken hata oluştu: {}", toEmail, e);
        }
    }

}
