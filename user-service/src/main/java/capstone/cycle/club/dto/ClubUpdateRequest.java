package capstone.cycle.club.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClubUpdateRequest {

    @NotBlank(message = "동호회 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "동호회 이름은 2~50자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "동호회 설명은 필수입니다.")
    @Size(max = 10000, message = "동호회 설명은 1000자를 넘을 수 없습니다.")
    private String description;

    @Min(value = 2, message = "최소 인원은 1명 이상이여야 합니다.")
    @Max(value = 100, message = "최대 인원은 100명을 넘을 수 없습니다.")
    private int maxMemberCount;

    @NotBlank(message = "활동 지역(도)는 필수입니다.")
    private String administrationArea;

    @NotBlank(message = "활동 지역(시)는 필수입니다.")
    private String city;
}
