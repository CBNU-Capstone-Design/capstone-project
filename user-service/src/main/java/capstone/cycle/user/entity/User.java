package capstone.cycle.user.entity;

import capstone.cycle.user.dto.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String socialId;

    @Column(nullable = false, unique = true)
    @NotNull
    private String email;

    @NotNull
    private String nickname;

    private String snsProfileImageUrl;

    private String role;

    @NotNull
    private String socialProvider;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "administrativeArea", column = @Column(name = "ADMINISTRATIVE_AREA")),
            @AttributeOverride(name = "locality", column = @Column(name = "LOCALITY")),
    })
    private Location currentLocation = Location.createDefaultLocation();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // 정적 팩토리 메서드
    public static User createUser(String socialId, String socialProvider, String email,
                                  String nickname, String role, String snsProfileImageUrl,
                                  String administrativeArea, String locality) {
        return User.builder()
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .snsProfileImageUrl(snsProfileImageUrl)
                .currentLocation(new Location(administrativeArea, locality))
                .build();
    }


    // 닉네임 업데이트
    public User withNickname(String newNickname) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(newNickname)
                .role(this.role)
                .currentLocation(this.currentLocation)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }



    // 현재 위치 업데이트
    public User withCurrentLocation(Location currentLocation) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .currentLocation(currentLocation)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    public UserDTO toDTO() {
        return UserDTO.builder()
                .userId(id)
                .nickname(nickname)
                .email(email)
                .location(currentLocation)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .role(role)
                .snsProfileImageUrl(snsProfileImageUrl)
                .build();
    }
    public User withRole(String newRole) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(newRole)
                .currentLocation(this.currentLocation)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
