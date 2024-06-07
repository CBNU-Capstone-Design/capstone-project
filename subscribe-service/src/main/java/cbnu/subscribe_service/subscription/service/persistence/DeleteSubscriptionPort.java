package cbnu.subscribe_service.subscription.service.persistence;

import cbnu.subscribe_service.subscription.domain.Subscription;

public interface DeleteSubscriptionPort {
    void delete(Subscription subscription);
}
