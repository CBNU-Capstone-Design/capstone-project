package cbnu.subscribe_service.point.controller;

import jakarta.validation.constraints.NotNull;

public record PresentPointRequest(@NotNull Long userId, @NotNull Long toUserId, @NotNull Long presentPoint) {
}
