package cbnu.subscribe_service.subscription.domain;

import lombok.Getter;

@Getter
public enum RefundRate {
    RATE(50L);

    private final Long rate;

    RefundRate(Long rate) {
        this.rate = rate;
    }
}
