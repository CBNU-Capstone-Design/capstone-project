package capstone.cycle.club.dto;

import capstone.cycle.user.dto.SimpleUserInfoDTO;
import capstone.cycle.user.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClubMemberInfoResponse {
    private SimpleUserInfoDTO clubMember;
    private String clubMemberRole;

    public static ClubMemberInfoResponse from(User user, String clubMemberRole) {
        return ClubMemberInfoResponse.builder()
                .clubMember(SimpleUserInfoDTO.from(user))
                .clubMemberRole(clubMemberRole)
                .build();
    }
}
