package capstone.cycle.club.entity.member;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ClubMemberId implements Serializable {
    private Long clubId;
    private Long userId;
}
