package capstone.cycle.user.dto;

import capstone.cycle.user.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DetailUserInfoDTO {
    private Long id;
    private String nickname;
    private String profileImageUrl;


    public static DetailUserInfoDTO from(User user) {
        return DetailUserInfoDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getSnsProfileImageUrl())
                .build();
    }
}
