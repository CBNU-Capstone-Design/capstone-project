package cbnu.subscribe_service.subscription.service.persistence;

import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.subscription.domain.Subscription;

public interface LoadSubscriptionPort {

    Subscription loadByUserId(UserId userId);
}
