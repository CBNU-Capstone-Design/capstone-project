package cbnu.subscribe_service.point.service.persistence;

import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.point.domain.PointId;

public interface LoadPointPort {

    Point load(PointId pointId);
}
