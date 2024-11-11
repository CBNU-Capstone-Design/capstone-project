package capstone.cycle.common.security.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorResult {
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    ACCESS_TOKEN_NEED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 필요합니다."),
    REFRESH_TOKEN_NEED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 필요합니다."),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "토큰을 전달해주세요"),
    LOGOUT_FAILED(HttpStatus.BAD_REQUEST, "로그아웃 처리에 실패했습니다."),
    TOKEN_ROTATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 갱신에 실패했습니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다. 다시 로그인해주세요."),
    TOKEN_INVALIDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 무효화 처리에 실패했습니다."),
    TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "토큰 재사용이 감지되었습니다. 보안을 위해 모든 세션이 종료됩니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_EXPIRED_OR_INVALID(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요."),
    ;

    private final HttpStatus status;
    private final String message;
}
