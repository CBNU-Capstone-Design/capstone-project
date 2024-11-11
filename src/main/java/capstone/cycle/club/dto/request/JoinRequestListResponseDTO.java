package capstone.cycle.club.dto.request;

import capstone.cycle.club.entity.request.ClubJoinRequest;
import capstone.cycle.club.entity.request.JoinRequestStatus;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JoinRequestListResponseDTO {
    private Long clubId;
    private SimpleUserInfoDTO user;
    private String message;
    private JoinRequestStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static JoinRequestListResponseDTO from(ClubJoinRequest request) {
        return JoinRequestListResponseDTO.builder()
                .clubId(request.getClub().getId())
                .user(SimpleUserInfoDTO.from(request.getUser()))
                .message(request.getMessage())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
