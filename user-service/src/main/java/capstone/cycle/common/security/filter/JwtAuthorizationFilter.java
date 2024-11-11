package capstone.cycle.common.security.filter;

import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.common.security.service.SecurityService;
import capstone.cycle.user.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SecurityService securityService;
    private final RedisTokenService redisTokenService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String accessToken = extractTokenFromHeader(request, "Authorization");

            if (accessToken != null) {
                // 블랙리스트 체크
                if (redisTokenService.isBlacklisted(accessToken)) {
                    log.warn("Blocked attempt to use blacklisted token");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                    return;
                }

                try {
                    if (jwtUtil.validateAccessToken(accessToken)) {
                        securityService.saveUserInSecurityContext(accessToken);
                    }
                } catch (Exception e) {
                    log.warn("Token validation failed: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String[] excludePath = {
                "/api-docs",
                "/api/u/v1/social-login",
                "/api/u/v1/token/refresh",
                "/swagger-ui/",
                "/error",
                "/api/n/v1/",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
        };
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
