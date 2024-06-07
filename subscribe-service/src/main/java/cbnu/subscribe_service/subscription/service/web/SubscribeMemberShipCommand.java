package cbnu.subscribe_service.subscription.service.web;

public record SubscribeMemberShipCommand(Long userId, String type, Long days) {
}
