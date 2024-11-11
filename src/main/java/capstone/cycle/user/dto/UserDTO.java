package capstone.cycle.user.dto;

import capstone.cycle.file.entity.File;
import capstone.cycle.user.entity.Location;
import capstone.cycle.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {
    private Long userId;
    private String socialId;
    private String socialProvider;
    private String email;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String snsProfileImageUrl;
    private Location location;

    public UserDTO(Long userId, String socialId, String socialProvider) {
        this.userId = userId;
        this.socialId = socialId;
        this.socialProvider = socialProvider;
    }
    public User toEntity(Supplier<File> fileSupplier) {
        User.UserBuilder entity = User.builder()
                .id(userId)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .currentLocation(location)
                .snsProfileImageUrl(snsProfileImageUrl);

        return entity.build();
    }

    public User toEntity() {
        User.UserBuilder userBuilder = User.builder()
                .id(userId)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .currentLocation(location)
                .snsProfileImageUrl(snsProfileImageUrl);

        return userBuilder.build();
    }


}
