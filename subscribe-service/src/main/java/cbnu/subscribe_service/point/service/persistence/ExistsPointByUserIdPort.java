package cbnu.subscribe_service.point.service.persistence;

import cbnu.subscribe_service.domain.UserId;

public interface ExistsPointByUserIdPort {
    boolean exist(UserId userId);
}
