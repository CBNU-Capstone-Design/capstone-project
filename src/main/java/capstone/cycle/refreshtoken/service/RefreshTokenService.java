package capstone.cycle.refreshtoken.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {
    private final RedisTokenService redisTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityMilliseconds;

    @Transactional
    public String createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        String token = generateRefreshToken();
        redisTokenService.saveRefreshToken(userId, token, refreshTokenValidityMilliseconds);
        return token;
    }

    @Transactional
    public Map<String, String> rotateRefreshToken(String oldRefreshToken) {
        try {
            // redis에서 userId 찾기
            Long userId = redisTokenService.findUserIdByRefreshToken(oldRefreshToken);

            // 토큰이 이미 사용된 것인지 확인
            if (redisTokenService.isTokenUsed(oldRefreshToken)) {
                log.warn("Refresh token reuse detected for user: {}", userId);
                revokeAllUserTokens(userId);
                throw new TokenException(TokenErrorResult.TOKEN_REUSE_DETECTED);
            }

            // 현재 토큰을 사용됨으로 표시
            redisTokenService.markTokenAsUsed(oldRefreshToken, refreshTokenValidityMilliseconds);

            // 새로운 토큰 발급
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

            String newRefreshToken = generateRefreshToken();
            String newAccessToken = jwtUtil.generateAccessToken(user.toDTO());

            // 새로운 RefreshToken을 redis에 저장
            redisTokenService.saveRefreshToken(userId, newRefreshToken, refreshTokenValidityMilliseconds);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            return tokens;
        } catch (Exception e) {
            log.error("Error rotating refresh token", e);
            throw new TokenException(TokenErrorResult.TOKEN_ROTATION_FAILED);
        }
    }

    @Transactional
    public void revokeRefreshToken(Long userId) {
        redisTokenService.deleteRefreshToken(userId);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        redisTokenService.deleteRefreshToken(userId);
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String extractUserIdFromToken(String token) {
        try {
            return String.valueOf(redisTokenService.findUserIdByRefreshToken(token));
        } catch (Exception e) {
            log.error("Error extracting user ID from refresh token", e);
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }
}
