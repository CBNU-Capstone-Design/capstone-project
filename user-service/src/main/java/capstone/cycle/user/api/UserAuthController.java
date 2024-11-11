package capstone.cycle.user.api;

import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.common.security.service.SecurityService;
import capstone.cycle.refreshtoken.service.RefreshTokenService;
import capstone.cycle.user.dto.SocialLoginDTO;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/u/v1")
@RequiredArgsConstructor
@Slf4j
public class UserAuthController {

    private final UserService userService;
    private final SecurityService securityService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/social-login")

    public ResponseEntity<Map<String, String>> socialLogin(
            @RequestBody @Valid SocialLoginDTO socialLoginDTO) {
        // 로그인/회원가입
        UserDTO userDTO = userService.socialLogin(socialLoginDTO);
        securityService.saveUserInSecurityContext(userDTO);

        // 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(userDTO);
        String refreshToken = refreshTokenService.createRefreshToken(userDTO.getUserId());

        // 응답 생성
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        log.info("User logged in successfully: {}", userDTO.getUserId());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        if (!refreshToken.startsWith("Bearer ")) {
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }

        String token = refreshToken.substring(7);
        Map<String, String> tokens = refreshTokenService.rotateRefreshToken(token);

        log.debug("Token refreshed successfully");
        return ResponseEntity.ok(tokens);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.logout(accessToken, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
