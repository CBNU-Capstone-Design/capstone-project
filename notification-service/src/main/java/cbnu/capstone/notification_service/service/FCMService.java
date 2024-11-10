package cbnu.capstone.notification_service.service;

import cbnu.capstone.notification_service.entity.FCMToken;
import cbnu.capstone.notification_service.repository.FCMTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FCMService {

    private final FCMTokenRepository fcmTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);

    private final FirebaseMessaging firebaseMessaging;

    public FCMService(FCMTokenRepository fcmTokenRepository, FirebaseMessaging firebaseMessaging) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.firebaseMessaging = firebaseMessaging;
        logger.info("FCMService initialized with FirebaseMessaging instance: {}", firebaseMessaging);
    }

    public void saveToken(String token, String userId) {
        FCMToken fcmToken = new FCMToken();
        fcmToken.setToken(token);
        fcmToken.setUserId(userId);
        fcmTokenRepository.save(fcmToken);
    }

    public void sendNotification(String title, String body) {
        fcmTokenRepository.findAll().forEach(fcmToken -> {
            logger.info("Attempting to send notification. Title: {}, Body: {}, Token: {}", title, body, fcmToken.getToken());

            Message message = Message.builder()
                    .setToken(fcmToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            try {
                String response = firebaseMessaging.send(message);
                logger.info("Successfully sent message: {}", response);
            } catch (Exception e) {
                logger.error("Failed to send FCM message", e);
            }
        });
    }
}