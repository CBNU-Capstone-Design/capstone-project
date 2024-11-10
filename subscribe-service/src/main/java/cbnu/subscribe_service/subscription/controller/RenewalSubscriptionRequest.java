package cbnu.subscribe_service.subscription.controller;

public record RenewalSubscriptionRequest(Long userId, Long days, String type) {
}
