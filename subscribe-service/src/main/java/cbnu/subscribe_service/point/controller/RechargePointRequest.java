package cbnu.subscribe_service.point.controller;

import jakarta.validation.constraints.NotNull;

public record RechargePointRequest(@NotNull Long userId, @NotNull Long point) {
}
