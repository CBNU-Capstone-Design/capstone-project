package cbnu.subscribe_service.subscription.domain;

import cbnu.subscribe_service.common.exception.SubscriptionExpiredException;
import cbnu.subscribe_service.common.exception.WrongUserIdException;
import cbnu.subscribe_service.common.generic.AggregateRoot;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.subscription.domain.SubscriptionId.SubscriptionIdJavaType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JavaType;

@Getter
@Entity
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends AggregateRoot<Subscription, SubscriptionId> {

    @Id
    @Column(name = "subscribe_id")
    @JavaType(SubscriptionIdJavaType.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private SubscriptionId subscriptionId;

    @NotNull
    @Embedded
    private SubscriptionTime subscriptionTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType type;

    @NotNull
    @Embedded
    private UserId userId;

    @Version
    private Long version;

    private Subscription(Long days, Long userId, String type) {
        this.userId = UserId.make(userId);
        this.subscriptionTime = new SubscriptionTime(days);
        this.type = SubscriptionType.valueOf(type);
    }

    public static Subscription apply(Point point, Long days, Long userId, String type) {
        SubscriptionType subscriptionType = SubscriptionType.valueOf(type);
        DiscountType discountType = DiscountType.daysRate(days);
        long price = subscriptionType.getPrice() * days * (100 - discountType.getRate()) / 100;
        point.usePoint(userId, price);
        return new Subscription(days, userId, type);
    }

    public Authorization verifyAuthentication(Long userId, String type) {
        verifyUserId(userId);
        SubscriptionType accessRights = SubscriptionType.valueOf(type);
        if (accessRights.getPrice() < this.type.getPrice()) {
            return Authorization.AUTHENTICATED;
        }
        return Authorization.UNAUTHENTICATED;
    }

    public Long refund(Long userId) {
        verifyUserId(userId);
        Long days = getDays();
        return days * type.getPrice() * RefundRate.RATE.getRate() / 100;
    }

    private Long getDays() {
        Instant endDate = subscriptionTime.getEndDate();
        Instant nowTime = Instant.now();
        verifyDate(endDate, nowTime);
        return Duration.between(nowTime, endDate).toDays();
    }

    private void verifyUserId(Long userId) {
        if (!Objects.equals(this.userId.getId(), userId)) {
            throw new WrongUserIdException("유저정보가 일치하지 않습니다");
        }
    }

    private void verifyDate(Instant endDate, Instant nowDate) {
        if (endDate.isBefore(nowDate)) {
            throw new SubscriptionExpiredException("구독권이 만료되었습니다");
        }
    }

    @Override
    public SubscriptionId getId() {
        return subscriptionId;
    }
}
