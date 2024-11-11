package cbnu.subscribe_service.subscription.controller;

public record SubscribeMemberShipRequest(Long userId, Long days, String type) {
}
