package cbnu.subscribe_service.point.service.web;

public record PresentPointCommand(Long userId, Long toUserId, Long sendPoint) {
}
