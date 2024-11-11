package capstone.cycle.club.entity.request;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ClubJoinRequestId implements Serializable {
    private Long clubId;
    private Long userId;
}
