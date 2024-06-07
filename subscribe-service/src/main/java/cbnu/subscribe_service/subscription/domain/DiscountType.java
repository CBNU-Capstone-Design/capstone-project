package cbnu.subscribe_service.subscription.domain;

import lombok.Getter;

@Getter
public enum DiscountType {
    UNDER(0L), ONE_MONTH(5L), TWO_MONTH(10L), THREE_MONTH(20L), EXCEED(30L);

    private final Long rate;

    DiscountType(Long rate) {
        this.rate = rate;
    }

    public static DiscountType daysRate(Long days) {
        long months = days / 30;
        if (months == 0) {
            return UNDER;
        } else if (months == 1) {
            return ONE_MONTH;
        } else if (months == 2) {
            return TWO_MONTH;
        } else if (months == 3) {
            return THREE_MONTH;
        }
        return EXCEED;

    }
}
