package capstone.cycle.club.entity.member;

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
@Builder
@Table(name = "club_member")
public class ClubMember {

    @EmbeddedId
    private ClubMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clubId")
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubMemberRole role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime joinedAt;

    // 정적 팩토리 메서드
    public static ClubMember createMember(Club club, User user, ClubMemberRole role) {
        return ClubMember.builder()
                .id(new ClubMemberId(club.getId(), user.getId()))
                .club(club)
                .user(user)
                .role(role)
                .build();
    }

    // 역할 변경
    public ClubMember updateRole(ClubMemberRole newRole) {
        return ClubMember.builder()
                .id(this.id)
                .club(this.club)
                .user(this.user)
                .role(newRole)
                .joinedAt(this.joinedAt)
                .build();
    }
}
