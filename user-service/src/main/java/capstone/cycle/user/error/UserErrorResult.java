package capstone.cycle.user.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorResult {
    ALREADY_EXIST(HttpStatus.CONFLICT, "이미 등록된 유저가 있습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "필수값이 누락되거나 부적절한 값이 반환되었습니다."),
    DUPLICATED_USER_REGISTER(HttpStatus.BAD_REQUEST, "기존에 등록된 유저입니다."),
    ALREADY_USED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다.")
    ;

    private final HttpStatus status;
    private final String message;

}
