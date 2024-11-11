package cbnu.capstone.notification_service.controller;

import cbnu.capstone.notification_service.dto.NotificationRequest;
import cbnu.capstone.notification_service.service.FCMService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FCMController {

    private final FCMService fcmService;

    @PostMapping("/token")
    public ResponseEntity<String> registerToken(@RequestBody Map<String, String> payload) {
        System.out.println("payload = " + payload);
        String token = payload.get("token");
        String userId = payload.get("userId");
        fcmService.saveToken(token,userId);
        return ResponseEntity.ok("Token registered successfully");
    }

    @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String body = payload.get("body");
        fcmService.sendNotification(title, body);
        return ResponseEntity.ok("Notification sent successfully");
    }
}