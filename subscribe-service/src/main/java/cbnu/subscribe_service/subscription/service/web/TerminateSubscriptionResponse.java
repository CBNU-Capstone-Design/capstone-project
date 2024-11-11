package cbnu.subscribe_service.subscription.service.web;

import lombok.Builder;

@Builder(toBuilder = true)
public record TerminateSubscriptionResponse(Long userId, Long previousPoint, Long refundPoint, Long currentPoint) {
}
