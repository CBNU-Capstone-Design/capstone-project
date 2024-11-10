package cbnu.subscribe_service.point.service.persistence;

import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;

public interface LoadPointByUserIdPort {

    Point loadByUserId(UserId userId);
}
