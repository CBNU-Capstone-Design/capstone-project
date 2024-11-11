package capstone.cycle.user.dto;

import capstone.cycle.user.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleUserInfoDTO {
    private Long id;
    private String nickname;
    private String profileImageUrl;

    public static SimpleUserInfoDTO from(User user) {
        return SimpleUserInfoDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getSnsProfileImageUrl())
                .build();
    }
}
