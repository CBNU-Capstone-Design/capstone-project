package cbnu.subscribe_service.subscription.domain;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    PREMIUM(1500L), STANDARD(1000L),BASIC(500L), FREE(0L);

    private final Long price;

    SubscriptionType(Long price) {
        this.price = price;
    }
}
