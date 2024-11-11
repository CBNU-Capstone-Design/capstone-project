package capstone.cycle.like.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LikeErrorResult {
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "비활성화된 계정으로는 이 작업을 수행할 수 없습니다."),
    BLOCKED_USER(HttpStatus.FORBIDDEN, "차단된 사용자는 이 작업을 수행할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
