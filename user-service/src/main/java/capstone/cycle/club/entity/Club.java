package capstone.cycle.club.entity;

import capstone.cycle.club.entity.member.ClubMember;
import capstone.cycle.club.entity.request.ClubJoinRequest;
import capstone.cycle.file.entity.File;
import capstone.cycle.user.entity.Location;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_image_id")
    private File clubImage;

    @Column(nullable = false)
    private int maxMemberCount;

    @Column(nullable = false)
    private int memberCount;

    @Embedded
    private Location activityArea;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubJoinRequest> joinRequests = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    //    private List<> tags; //태그
//    private String ageRange; //연령대 //추가할지 말지


    // 정적 팩토리 메서드
    public static Club createClub(String name, String description, User leader,
                                  int maxMemberCount, Location activityArea) {
        return Club.builder()
                .name(name)
                .description(description)
                .leader(leader)
                .maxMemberCount(maxMemberCount)
                .memberCount(1) // 리더가 첫 번째 멤버
                .activityArea(activityArea)
                .members(new ArrayList<>())
                .joinRequests(new ArrayList<>())
                .build();
    }

    // 회원 수 증가
    public Club incrementMemberCount() {
        if (this.memberCount >= this.maxMemberCount) {
            throw new IllegalStateException("Club has reached maximum capacity");
        }
        return this.toBuilder()
                .memberCount(this.memberCount + 1)
                .build();
    }

    // 회원 수 감소
    public Club decrementMemberCount() {
        return this.toBuilder()
                .memberCount(Math.max(0, this.memberCount - 1))
                .build();
    }

    public boolean isFull() {
        return this.memberCount >= this.maxMemberCount;
    }

    public Club withClubImage(File clubImage) {
        return this.toBuilder()
                .clubImage(clubImage)
                .build();
    }

    public Club withLeader(User newLeader) {
        return this.toBuilder()
                .leader(newLeader)
                .build();
    }

    public Club update(
            String name,
            String description,
            int maxMemberCount,
            Location activityArea,
            File clubImage
    ) {
        return this.toBuilder()
                .name(name)
                .description(description)
                .maxMemberCount(maxMemberCount)
                .activityArea(activityArea)
                .clubImage(clubImage)  // null이면 기존 이미지 제거
                .build();
    }
}
