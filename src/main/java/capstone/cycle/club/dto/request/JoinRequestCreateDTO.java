package capstone.cycle.club.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JoinRequestCreateDTO {
    @NotBlank(message = "가입 신청 메시지를 입력해주세요")
    @Size(max = 500, message = "가입 신청 메시지는 최대 500자까지 입력 가능합니다.")
    private String message;
}
