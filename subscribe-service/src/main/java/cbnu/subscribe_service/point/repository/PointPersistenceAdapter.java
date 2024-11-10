package cbnu.subscribe_service.point.repository;

import cbnu.subscribe_service.common.annotation.PersistenceAdapter;
import cbnu.subscribe_service.common.exception.NotFoundDataException;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.point.domain.PointId;
import cbnu.subscribe_service.point.service.persistence.ExistsPointByUserIdPort;
import cbnu.subscribe_service.point.service.persistence.LoadPointByUserIdPort;
import cbnu.subscribe_service.point.service.persistence.LoadPointPort;
import cbnu.subscribe_service.point.service.persistence.RegisterPointPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
class PointPersistenceAdapter implements RegisterPointPort, LoadPointPort, LoadPointByUserIdPort,
        ExistsPointByUserIdPort {

    private final PointRepository pointRepository;

    @Override
    public void register(Point point) {
        pointRepository.save(point);
    }

    @Override
    public Point load(PointId pointId) {
        return pointRepository.findById(pointId).orElseThrow(() -> new NotFoundDataException("해당 Point를 찾을 수 없습니다"));
    }

    @Override
    public Point loadByUserId(UserId userId) {
        return pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new NotFoundDataException("해당 Point를 찾을 수 없습니다"));
    }

    @Override
    public boolean exist(UserId userId) {
        return pointRepository.existsByUserId(userId);
    }
}
