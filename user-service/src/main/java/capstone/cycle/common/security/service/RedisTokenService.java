package capstone.cycle.common.security.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String USED_TOKEN_PREFIX = "USED:";
    private static final String BLACKLIST_PREFIX = "BL:";


    // RefreshToken 저장
    public void saveRefreshToken(Long userId, String refreshToken, long ttl) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.MILLISECONDS);
            log.info("Successfully saved refresh token for user: {}", userId);
        } catch (Exception e) {
            log.error("Error saving refresh token for user: {}", userId, e);
            throw e;
        }
    }

    // RefreshToken 조회
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);
        log.debug("Retrieved refresh token for user: {}, exists: {}", userId, token != null);
        return token;
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);
        log.info("Deleted refresh token for user: {}, success: {}", userId, deleted);
    }

    // 사용된 RefreshToken으로 표시
    public void markTokenAsUsed(String token, long ttl) {
        String key = USED_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
        log.debug("Marked refresh token as used: {}", token);
    }

    // RefreshToken이 이미 사용되었는지 확인
    public boolean isTokenUsed(String token) {
        String key = USED_TOKEN_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // AccessToken을 블랙리스트에 추가
    public void addToBlacklist(String accessToken, long ttl) {
        String key = BLACKLIST_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
            log.info("Added access token to blacklist, expires in {} ms", ttl);
        } catch (Exception e) {
            log.error("Error adding token to blacklist", e);
            throw e;
        }
    }

    // AccessToken이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // 만료된 토큰들 정리
    public void cleanupExpiredTokens() {
        try {
            // 사용된 토큰 정리
            Set<String> usedTokens = redisTemplate.keys(USED_TOKEN_PREFIX + "*");
            if (usedTokens != null && !usedTokens.isEmpty()) {
                redisTemplate.delete(usedTokens);
                log.info("Cleaned up {} used tokens", usedTokens.size());
            }

            // 블랙리스트 토큰 정리
            Set<String> blacklistedTokens = redisTemplate.keys(BLACKLIST_PREFIX + "*");
            if (blacklistedTokens != null && !blacklistedTokens.isEmpty()) {
                redisTemplate.delete(blacklistedTokens);
                log.info("Cleaned up {} blacklisted tokens", blacklistedTokens.size());
            }

            // 활성 RefreshToken 개수 로깅
            Set<String> activeTokens = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");
            if (activeTokens != null) {
                log.info("Current active refresh tokens: {}", activeTokens.size());
            }
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
            throw e;
        }
    }

    // refresh token으로 userId를 찾음
    public Long findUserIdByRefreshToken(String refreshToken) {
        String storedToken = null;
        Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");

        if (keys != null) {
            for (String key : keys) {
                storedToken = redisTemplate.opsForValue().get(key);
                if (refreshToken.equals(storedToken)) {
                    return Long.parseLong(key.substring(REFRESH_TOKEN_PREFIX.length()));
                }
            }
        }

        // 저장된 토큰이 없다는 것은 이미 로그아웃 되었거나 만료된 토큰
        log.warn("Invalid or expired refresh token used");
        throw new TokenException(TokenErrorResult.REFRESH_TOKEN_EXPIRED_OR_INVALID);
    }

    /**
     * 로그아웃 시 토큰 무효화 처리
     */
    public void blacklistAndInvalidateTokens(String accessToken, Long userId) {
        try {
            // AccessToken을 블랙리스트에 추가
            if (accessToken != null && accessToken.startsWith("Bearer ")) {
                String token = accessToken.substring(7);
                long remainingTime = jwtUtil.getRemainingTime(token);
                if (remainingTime > 0) {
                    addToBlacklist(token, remainingTime);
                    log.info("Added access token to blacklist for user: {}", userId);
                }
            }

            // RefreshToken 삭제
            deleteRefreshToken(userId);
            log.info("Successfully invalidated all tokens for user: {}", userId);
        } catch (Exception e) {
            log.error("Error during token invalidation for user: {}", userId, e);
            throw new TokenException(TokenErrorResult.LOGOUT_FAILED);
        }
    }
}
