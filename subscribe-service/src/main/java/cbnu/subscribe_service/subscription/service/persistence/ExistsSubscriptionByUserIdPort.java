package cbnu.subscribe_service.subscription.service.persistence;

import cbnu.subscribe_service.domain.UserId;

public interface ExistsSubscriptionByUserIdPort {

    boolean exists(UserId userId);
}
