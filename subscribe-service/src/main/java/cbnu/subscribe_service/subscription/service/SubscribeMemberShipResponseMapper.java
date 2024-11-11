package cbnu.subscribe_service.subscription.service;

import cbnu.subscribe_service.subscription.domain.Subscription;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipResponse;
import java.time.LocalDate;
import java.time.ZoneId;

class SubscribeMemberShipResponseMapper {

    static SubscribeMemberShipResponse convert(Subscription subscription) {
        return SubscribeMemberShipResponse.builder()
                .subscriptionId(subscription.getSubscriptionId().getId())
                .startDate(
                        LocalDate.ofInstant(subscription.getSubscriptionTime().getStartDate(), ZoneId.of("Asia/Seoul")))
                .endDate(
                        LocalDate.ofInstant(subscription.getSubscriptionTime().getEndDate(), ZoneId.of("Asia/Seoul")))
                .subscriptionType(subscription.getType())
                .userId(subscription.getUserId().getId())
                .build();
    }
}
