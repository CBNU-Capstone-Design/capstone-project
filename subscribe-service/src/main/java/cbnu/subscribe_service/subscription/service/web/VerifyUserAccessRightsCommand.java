package cbnu.subscribe_service.subscription.service.web;

import cbnu.subscribe_service.subscription.domain.SubscriptionType;

public record VerifyUserAccessRightsCommand(Long userId, String type) {
}
