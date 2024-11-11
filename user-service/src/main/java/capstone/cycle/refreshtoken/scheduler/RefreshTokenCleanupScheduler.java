package capstone.cycle.refreshtoken.scheduler;

import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.refreshtoken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RedisTokenService redisTokenService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void cleanupExpiredTokens() {
        try {
            log.info("Starting scheduled cleanup of expired tokens");
            redisTokenService.cleanupExpiredTokens();
            log.info("Completed scheduled cleanup of expired tokens");
        } catch (Exception e) {
            log.error("Error during scheduled token cleanup", e);
        }
    }
}
