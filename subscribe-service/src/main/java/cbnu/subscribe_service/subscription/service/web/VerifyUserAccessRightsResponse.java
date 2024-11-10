package cbnu.subscribe_service.subscription.service.web;

import cbnu.subscribe_service.subscription.domain.Authorization;

public record VerifyUserAccessRightsResponse(Long userId, Authorization authorization) {
}
