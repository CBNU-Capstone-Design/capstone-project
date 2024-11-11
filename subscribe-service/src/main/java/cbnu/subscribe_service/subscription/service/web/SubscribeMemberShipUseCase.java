package cbnu.subscribe_service.subscription.service.web;

import cbnu.subscribe_service.subscription.domain.Subscription;

public interface SubscribeMemberShipUseCase {

    SubscribeMemberShipResponse subscribe(SubscribeMemberShipCommand subscribeMemberShipCommand);
}
