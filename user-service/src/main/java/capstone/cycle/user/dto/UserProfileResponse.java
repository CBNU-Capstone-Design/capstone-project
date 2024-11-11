package capstone.cycle.user.dto;

import capstone.cycle.club.dto.ClubSummaryDTO;
import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileResponse {

    private DetailUserInfoDTO userInfo;
    private List<ClubSummaryDTO> clubs;

}
