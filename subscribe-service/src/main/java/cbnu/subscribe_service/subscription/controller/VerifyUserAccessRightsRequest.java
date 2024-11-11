package cbnu.subscribe_service.subscription.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyUserAccessRightsRequest(@NotNull Long userId, @NotBlank String type) {
}
