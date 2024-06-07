package cbnu.subscribe_service.subscription.domain;

import cbnu.subscribe_service.common.generic.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionTime extends ValueObject<SubscriptionTime> {

    @Column(name = "start_date", columnDefinition = "date")
    private Instant startDate;

    @Column(name = "end_date", columnDefinition = "date")
    private Instant endDate;

    public SubscriptionTime(long days) {
        startDate = Instant.now();
        endDate = startDate.plus(days, ChronoUnit.DAYS);
    }
}
