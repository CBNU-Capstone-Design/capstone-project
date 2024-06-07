package cbnu.subscribe_service.point.controller;

import jakarta.validation.constraints.NotNull;

public record RegisterPointRequest(@NotNull Long userId) {
}
