package capstone.cycle.user.dto;

import capstone.cycle.user.annotation.IsSocialProvider;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SocialLoginDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "socialId는 빈 값일 수 없습니다")
    private String socialId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "KAKAO / NAVER / GOOGLE", example = "KAKAO")
    @IsSocialProvider
    private String socialProvider;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 빈 값일 수 없습니다")
    private String nickname;

    @Hidden
    private String role;

    @Email(message = "올바른 이메일 형식이어야 합니다")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String snsProfileImageUrl;

    private String administrativeArea;

    private String locality;

//    private MultipartFile profile;

    // 정적 팩토리 메서드

}
