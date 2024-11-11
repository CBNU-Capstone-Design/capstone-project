package capstone.cycle.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNicknameDTO {

    @NotBlank
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    private String nickname;

    public static UpdateNicknameDTO of(String nickname) {
        return UpdateNicknameDTO.builder()
                .nickname(nickname)
                .build();
    }
}
