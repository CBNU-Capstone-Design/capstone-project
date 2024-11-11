package cbnu.subscribe_service.subscription.service.web;

import cbnu.subscribe_service.subscription.domain.SubscriptionType;
import java.time.LocalDate;
import lombok.Builder;

@Builder(toBuilder = true)
public record LoadSubscriptionResponse(Long userId, SubscriptionType subscriptionType, LocalDate startDate, LocalDate endDate) {
}
