package com.abonex.abonexbackend.service.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class FCMService {

    public void sendNotificationWithData(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(data)
                    .build();
            FirebaseMessaging.getInstance().send(message);
            log.info("Bildirim başarıyla gönderildi: Token={}", token);
        } catch (FirebaseMessagingException e) {
            log.error("Bildirim gönderilemedi: Token={}", token, e);
        }
    }
}