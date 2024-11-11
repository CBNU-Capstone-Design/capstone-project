package cbnu.subscribe_service.point.domain;

import cbnu.subscribe_service.common.exception.PointBelowThresholdException;
import cbnu.subscribe_service.common.exception.PointLimitExceededException;
import cbnu.subscribe_service.common.generic.ValueObject;
import cbnu.subscribe_service.domain.Threshold;
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
public class Money extends ValueObject<Money> {

    @Column(name = "total_point")
    private Long totalPoint;

    public static Money make(Long totalPoint) {
        return new Money(totalPoint);
    }

    public Money recharge(Long point) {
        exceedPointCheck(point);
        return new Money(this.totalPoint + point);
    }

    private void exceedPointCheck(Long point) {
        if (totalPoint + point > Threshold.MAX.getValue()) {
            throw new PointLimitExceededException("포인트 충전 값이 최대 값을 초과했습니다");
        }
    }

    public Money use(Long point) {
        belowPointCheck(point);
        return new Money(this.totalPoint - point);
    }

    private void belowPointCheck(Long point) {
        if (totalPoint - point < Threshold.MIN.getValue()) {
            throw new PointBelowThresholdException("현재 보유 포인트를 초과했습니다.");
        }
    }


}
