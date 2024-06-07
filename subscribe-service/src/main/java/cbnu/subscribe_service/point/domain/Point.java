package cbnu.subscribe_service.point.domain;

import cbnu.subscribe_service.common.exception.WrongUserIdException;
import cbnu.subscribe_service.common.generic.AggregateRoot;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.PointId.PointIdJavaType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JavaType;

@Getter
@Entity
@Table(name = "point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends AggregateRoot<Point, PointId> {

    @Id
    @Column(name = "point_id")
    @JavaType(PointIdJavaType.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private PointId pointId;

    @NotNull
    @Embedded
    private UserId userId;

    @NotNull
    @Embedded
    private Money money;

    @Version
    private Long version;

    private Point(Money money, UserId userId) {
        this.money = money;
        this.userId = userId;
    }

    public static Point make(Long userId) {
        Money money = Money.make(0L);
        return new Point(money, UserId.make(userId));

    }

    public void rechargePoint(Long userId, Long point) {
        verifyUserId(userId);
        this.money = this.money.recharge(point);
    }

    public void usePoint(Long userId, Long point) {
        verifyUserId(userId);
        this.money = this.money.use(point);
    }

    public Long presentPoint(Point toPoint, Long presentPoint) {
        usePoint(userId.getId(), presentPoint);
        toPoint.rechargePoint(toPoint.getUserId().getId(),presentPoint);
        return money.getTotalPoint();
    }

    private void verifyUserId(Long userId) {
        if (!Objects.equals(this.userId.getId(), userId)) {
            throw new WrongUserIdException("유저정보가 일치하지 않습니다");
        }
    }

    @Override
    public PointId getId() {
        return pointId;
    }
}
