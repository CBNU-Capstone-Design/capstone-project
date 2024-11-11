package capstone.cycle.club.dto;

import capstone.cycle.club.entity.Club;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubDetailResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String leaderName;
    private String clubImageUrl;
    private int memberCount;
    private int maxMemberCount;
    private String activityArea;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    private List<ClubMemberInfoResponse> members;

    public static ClubDetailResponseDTO from(Club club) {
        return ClubDetailResponseDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .leaderName(club.getLeader().getNickname())
                .clubImageUrl(club.getClubImage() != null ? club.getClubImage().getPath() : null)
                .memberCount(club.getMemberCount())
                .maxMemberCount(club.getMaxMemberCount())
                .activityArea(club.getActivityArea().getLocality())
                .createdAt(club.getCreatedAt())
                .members(club.getMembers().stream()
                        .map(member -> ClubMemberInfoResponse.from(member.getUser(), member.getRole().toString()))
                        .collect(Collectors.toList())
                )
                .build();
    }
}
