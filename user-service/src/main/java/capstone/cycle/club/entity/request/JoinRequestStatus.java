package capstone.cycle.club.entity.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JoinRequestStatus {

    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨");
    ;

    private final String displayName;
}
