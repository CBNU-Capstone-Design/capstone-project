package capstone.cycle.common.security.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.refreshtoken.service.RefreshTokenService;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    @Override
    public void saveUserInSecurityContext(UserDTO userDTO) {
        try {
            UserDetails userDetails = loadUserById(userDTO.getUserId());
            saveAuthentication(userDetails);
            log.debug("Successfully saved user in security context: {}", userDTO.getUserId());
        } catch (Exception e) {
            log.error("Failed to save user in security context", e);
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }

    @Override
    public void saveUserInSecurityContext(String accessToken) {
        try {
            // 블랙리스트 체크
            if (redisTokenService.isBlacklisted(accessToken)) {
                log.warn("Attempt to use blacklisted token");
                throw new TokenException(TokenErrorResult.TOKEN_BLACKLISTED);
            }

            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            UserDetails userDetails = loadUserById(userId);
            saveAuthentication(userDetails);
            log.debug("Successfully saved user in security context from token");
        } catch (TokenException e) {
            log.error("Token validation failed", e);
            SecurityContextHolder.clearContext();
            throw e;
        } catch (Exception e) {
            log.error("Failed to save user in security context", e);
            SecurityContextHolder.clearContext();
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }

    @Override
    public UserDTO getUserInfoSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            User user = ((UserDetailsImpl) authentication.getPrincipal()).getUser();
            return user.toDTO();
        }
        log.warn("No user found in security context");
        return null;
    }

    private void saveAuthentication(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        log.debug("Authentication saved in security context");
    }

    private UserDetails loadUserById(Long userId) {
        log.debug("Loading user by userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for userId: {}", userId);
                    return new TokenException(TokenErrorResult.TOKEN_EXPIRED);
                });

        String role = user.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
            user = user.withRole(role);
            userRepository.save(user);
            log.debug("Updated user role format to: {}", role);
        }

        return UserDetailsImpl.from(user);
    }

    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        log.debug("Security context cleared");
    }

    public Long getCurrentUserId() {
        UserDTO userDTO = getUserInfoSecurityContext();
        if (userDTO == null) {
            log.warn("No authenticated user found");
            throw new TokenException(TokenErrorResult.ACCESS_TOKEN_NEED);
        }
        return userDTO.getUserId();
    }
}
