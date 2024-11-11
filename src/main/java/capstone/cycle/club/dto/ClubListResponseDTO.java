package capstone.cycle.club.dto;

import capstone.cycle.club.entity.Club;
import lombok.*;


/**
 *
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ClubListResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String leaderName;
    private int memberCount;
    private String city;  // locality(시) 정보만 표시
    private String clubImageUrl;

    public static ClubListResponseDTO from(Club club) {
        return ClubListResponseDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .leaderName(club.getLeader().getNickname())
                .memberCount(club.getMemberCount())
                .city(club.getActivityArea().getLocality())
                .clubImageUrl(club.getClubImage() != null ? club.getClubImage().getPath() : null)
                .build();
    }

}
