package cbnu.subscribe_service.subscription.service.web;

public record RenewalSubscriptionCommand(Long userId, String type, Long days) {
}
