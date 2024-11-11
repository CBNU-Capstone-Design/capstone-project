package cbnu.subscribe_service.domain;

import cbnu.subscribe_service.common.exception.WrongThresholdRangeException;
import cbnu.subscribe_service.common.generic.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserId extends ValueObject<UserId> {

    @Column(name = "user_id")
    private Long id;

    public static UserId make(Long userId) {
        verifyUserId(userId);
        return new UserId(userId);
    }

    private static void verifyUserId(Long userId) {
        if (userId != null && userId <= Threshold.MIN.getValue()) {
            throw new WrongThresholdRangeException("userId가 1 미만입니다");
        }
    }
}
