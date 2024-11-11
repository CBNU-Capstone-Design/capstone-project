package cbnu.subscribe_service.subscription.repository;

import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.subscription.domain.Subscription;
import cbnu.subscribe_service.subscription.domain.SubscriptionId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    Optional<Subscription> findByUserId(UserId userId);

    boolean existsByUserId(UserId userId);
}
