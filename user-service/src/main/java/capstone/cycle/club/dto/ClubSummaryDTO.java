package capstone.cycle.club.dto;

import capstone.cycle.club.entity.Club;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClubSummaryDTO {

    private Long clubId;
    private String clubName;
    private String clubImageUrl;

    public static ClubSummaryDTO of(
            Club club
    ) {
        return ClubSummaryDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .clubImageUrl(club.getClubImage() != null ? club.getClubImage().getPath() : null)
                .build();
    }
}
