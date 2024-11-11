package capstone.cycle.club.entity.request;

import capstone.cycle.club.entity.Club;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Table(name = "club_join_request")
public class ClubJoinRequest {

    @EmbeddedId
    private ClubJoinRequestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clubId")
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static ClubJoinRequest createRequest(Club club, User user, String message) {
        return ClubJoinRequest.builder()
                .id(new ClubJoinRequestId(club.getId(), user.getId()))
                .club(club)
                .user(user)
                .message(message)
                .status(JoinRequestStatus.PENDING)
                .build();
    }

    public ClubJoinRequest accept() {
        return this.toBuilder()
                .status(JoinRequestStatus.ACCEPTED)
                .build();
    }

    public ClubJoinRequest reject() {
        return this.toBuilder()
                .status(JoinRequestStatus.REJECTED)
                .build();
    }
}
