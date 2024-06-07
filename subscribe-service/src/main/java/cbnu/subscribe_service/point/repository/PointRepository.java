package cbnu.subscribe_service.point.repository;

import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.point.domain.PointId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PointRepository extends JpaRepository<Point, PointId> {

    Optional<Point> findPointByUserId(UserId userId);
}
