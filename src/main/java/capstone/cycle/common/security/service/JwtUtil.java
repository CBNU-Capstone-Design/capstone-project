package capstone.cycle.common.security.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.refreshtoken.entity.RefreshToken;
import capstone.cycle.refreshtoken.service.RefreshTokenService;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKeyPlain;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMilliseconds; // 30분

    private Key secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = secretKeyPlain.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateAccessToken(UserDTO userDTO) {
        log.debug("Generating access token for user: {}", userDTO.getUserId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDTO.getUserId().toString());
        claims.put("role", userDTO.getRole());

        return createToken(claims, userDTO.getUserId().toString(), accessTokenValidityInMilliseconds);
    }

    private String createToken(Map<String, Object> claims, String subject, long validity) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + validity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new TokenException(TokenErrorResult.ACCESS_TOKEN_NEED);
            }

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Access token has expired");
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Token validation error", e);
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }

    public UserDTO getUserDetailsFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return UserDTO.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .role(claims.get("role", String.class))
                .build();
    }


    // extractAllClaims 메서드는 extractClaim을 사용하도록 수정
    private Claims extractAllClaims(String token) {
        return extractClaim(token, claims -> claims);
    }


    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> Long.parseLong(claims.getSubject()));
    }

    // getRemainingTime도 extractClaim을 사용하도록 수정
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            long now = System.currentTimeMillis();
            return Math.max(0, expiration.getTime() - now);
        } catch (Exception e) {
            return 0;
        }
    }

    public Long extractUserIdFromToken(String token) {
        return extractClaim(token, claims ->
                Long.parseLong(claims.get("userId", String.class)));
    }

    private UserDTO createUserDTOFromClaims(Claims claims) {
        return UserDTO.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .role(claims.get("role", String.class))
                .build();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from token");
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            T result = claimsResolver.apply(claims);
            log.debug("Successfully extracted claim from token");
            return result;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired while extracting claim");
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Error extracting claim from token", e);
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }
}
