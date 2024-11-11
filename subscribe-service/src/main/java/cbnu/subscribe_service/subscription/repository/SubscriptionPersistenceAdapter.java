package cbnu.subscribe_service.subscription.repository;

import cbnu.subscribe_service.common.annotation.PersistenceAdapter;
import cbnu.subscribe_service.common.exception.NotFoundDataException;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.subscription.domain.Subscription;
import cbnu.subscribe_service.subscription.service.persistence.DeleteSubscriptionPort;
import cbnu.subscribe_service.subscription.service.persistence.ExistsSubscriptionByUserIdPort;
import cbnu.subscribe_service.subscription.service.persistence.LoadSubscriptionPort;
import cbnu.subscribe_service.subscription.service.persistence.RegisterSubscriptionPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
class SubscriptionPersistenceAdapter implements RegisterSubscriptionPort, LoadSubscriptionPort, DeleteSubscriptionPort,
        ExistsSubscriptionByUserIdPort {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription register(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription loadByUserId(UserId userId) {
        return subscriptionRepository.findByUserId(userId).orElseThrow(()-> new NotFoundDataException("해당 유저의 구독권을 찾을 수 없습니다."));
    }

    @Override
    public void delete(Subscription subscription) {
        subscriptionRepository.delete(subscription);
    }

    @Override
    public boolean exists(UserId userId) {
        return subscriptionRepository.existsByUserId(userId);
    }
}
