package cbnu.capstone.notification_service.repository;

import cbnu.capstone.notification_service.entity.FCMToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    Optional<FCMToken> findByUserId(String userId);
}
