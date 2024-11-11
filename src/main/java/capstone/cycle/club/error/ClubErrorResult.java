package capstone.cycle.club.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClubErrorResult {

    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 동호회입니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다. 동호회장만 가능합니다."),
    CLUB_FULL(HttpStatus.BAD_REQUEST, "동호회가 정원이 다 찼습니다."),
    ALREADY_MEMBER(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다."),
    ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "이미 가입 신청한 상태입니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 가입 신청입니다."),
    INVALID_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 가입 신청 상태입니다."),
    INVALID_CITY_FORMAT(HttpStatus.BAD_REQUEST, "올바른 시 형식이 아닙니다. (예: 청주시, 서울시)"),
    MAX_CLUB_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 가입 가능한 동호회 수를 초과했습니다. (최대 3개)"),
    NOT_CLUB_MEMBER(HttpStatus.FORBIDDEN, "동호회 회원이 아닙니다."),
    ALREADY_MANAGER(HttpStatus.BAD_REQUEST, "이미 운영진인 회원입니다."),
    NOT_MANAGER(HttpStatus.BAD_REQUEST, "해당 회원은 운영진이 아닙니다."),
    CANNOT_EXPEL_LEADER(HttpStatus.BAD_REQUEST, "동호회장은 강퇴할 수 없습니다."),
    CANNOT_CHANGE_LEADER_ROLE(HttpStatus.BAD_REQUEST, "동호회장의 역할은 변경할 수 없습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "동호회장은 위임 후 탈퇴해야 합니다."),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    CLUB_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 동호회 이름입니다.")
    ;

    private final HttpStatus status;
    private final String message;

}
