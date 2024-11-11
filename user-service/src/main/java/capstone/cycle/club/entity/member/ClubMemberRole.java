package capstone.cycle.club.entity.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClubMemberRole {
    LEADER("동호회장"),
    MANAGER("운영진"),
    MEMBER("회원");

    private final String displayName;
}
