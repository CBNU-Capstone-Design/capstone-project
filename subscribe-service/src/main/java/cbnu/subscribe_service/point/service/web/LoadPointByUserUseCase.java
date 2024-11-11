package cbnu.subscribe_service.point.service.web;

import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;

public interface LoadPointByUserUseCase {

    Point load(UserId userId);
}
