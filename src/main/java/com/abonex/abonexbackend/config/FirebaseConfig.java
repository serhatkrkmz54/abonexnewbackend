package com.abonex.abonexbackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-file}")
    private Resource serviceAccount;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

}
