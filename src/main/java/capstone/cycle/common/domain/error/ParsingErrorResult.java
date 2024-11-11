package capstone.cycle.common.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParsingErrorResult {

    PARSING_FAIL(HttpStatus.BAD_REQUEST, "데이 변환에 실패하였습니다."),
    ;

    private final HttpStatus status;
    private final String message;

}
