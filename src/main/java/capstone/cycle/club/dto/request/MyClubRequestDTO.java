package capstone.cycle.club.dto.request;

import capstone.cycle.club.entity.Club;
import capstone.cycle.club.entity.request.ClubJoinRequest;
import capstone.cycle.club.entity.request.JoinRequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MyClubRequestDTO {

    private Long clubId;
    private String clubName;
    private String clubImageUrl;
    private JoinRequestStatus status;    // 신청 상태 (PENDING, ACCEPTED 등)
    private String message;              // 내가 작성한 가입 신청 메시지
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime requestedAt;   // 신청 일시

    public static MyClubRequestDTO from(ClubJoinRequest request) {
        Club club = request.getClub();
        return MyClubRequestDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .clubImageUrl(club.getClubImage() != null ? club.getClubImage().getPath() : null)
                .status(request.getStatus())
                .message(request.getMessage())
                .requestedAt(request.getCreatedAt())
                .build();
    }
}
