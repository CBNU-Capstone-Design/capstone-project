package cbnu.capstone.notification_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FCMConfig {

    private static final Logger logger = LoggerFactory.getLogger(FCMConfig.class);

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        logger.info("Initializing Firebase application...");

        InputStream serviceAccount = new ClassPathResource("firebase-service-cycleapp.json").getInputStream();
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.fromStream(serviceAccount);
            logger.info("Successfully loaded Google credentials");
        } catch (IOException e) {
            logger.error("Failed to load Google credentials", e);
            throw e;
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            logger.info("Initializing new Firebase application");
            return FirebaseApp.initializeApp(options);
        } else {
            logger.info("Firebase application already initialized, returning existing instance");
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        logger.info("Creating FirebaseMessaging bean");
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
