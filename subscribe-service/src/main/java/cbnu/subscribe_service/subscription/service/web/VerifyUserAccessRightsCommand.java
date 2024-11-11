package cbnu.subscribe_service.subscription.service.web;

public record VerifyUserAccessRightsCommand(Long userId, String type) {
}
