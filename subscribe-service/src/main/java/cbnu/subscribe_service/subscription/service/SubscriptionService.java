package cbnu.subscribe_service.subscription.service;

import cbnu.subscribe_service.common.annotation.UseCase;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.point.service.web.LoadPointByUserUseCase;
import cbnu.subscribe_service.subscription.domain.Authorization;
import cbnu.subscribe_service.subscription.domain.Subscription;
import cbnu.subscribe_service.subscription.service.persistence.DeleteSubscriptionPort;
import cbnu.subscribe_service.subscription.service.persistence.ExistsSubscriptionByUserIdPort;
import cbnu.subscribe_service.subscription.service.persistence.LoadSubscriptionPort;
import cbnu.subscribe_service.subscription.service.persistence.RegisterSubscriptionPort;
import cbnu.subscribe_service.subscription.service.web.LoadSubscriptionQuery;
import cbnu.subscribe_service.subscription.service.web.LoadSubscriptionResponse;
import cbnu.subscribe_service.subscription.service.web.RenewalSubscriptionCommand;
import cbnu.subscribe_service.subscription.service.web.RenewalSubscriptionUseCase;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipCommand;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipResponse;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipUseCase;
import cbnu.subscribe_service.subscription.service.web.TerminateSubscriptionResponse;
import cbnu.subscribe_service.subscription.service.web.TerminateSubscriptionUseCase;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsCommand;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsResponse;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsUseCase;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
@RequiredArgsConstructor
class SubscriptionService implements SubscribeMemberShipUseCase, LoadSubscriptionQuery, TerminateSubscriptionUseCase,
        VerifyUserAccessRightsUseCase, RenewalSubscriptionUseCase {

    private final LoadPointByUserUseCase loadPointByUserUseCase;
    private final RegisterSubscriptionPort subscriptionPort;
    private final LoadSubscriptionPort loadSubscriptionPort;
    private final DeleteSubscriptionPort deleteSubscriptionPort;
    private final ExistsSubscriptionByUserIdPort existsSubscriptionByUserIdPort;

    @Override
    public SubscribeMemberShipResponse subscribe(SubscribeMemberShipCommand subscribeMemberShipCommand) {
        Point point = loadPointByUserUseCase.load(UserId.make(subscribeMemberShipCommand.userId()));
        if (!existsSubscriptionByUserIdPort.exists(UserId.make(subscribeMemberShipCommand.userId()))) {
            Subscription subscription = Subscription.apply(point, subscribeMemberShipCommand.days(),
                    subscribeMemberShipCommand.userId(), subscribeMemberShipCommand.type());
            subscription = subscriptionPort.register(subscription);
            return SubscribeMemberShipResponseMapper.convert(subscription);
        }
        return null;
    }

    @Override
    public LoadSubscriptionResponse load(Long userId) {
        Subscription subscription = loadSubscriptionPort.loadByUserId(UserId.make(userId));
        return LoadSubscriptionResponse.builder()
                .userId((subscription.getUserId().getId()))
                .subscriptionType(subscription.getType())
                .startDate(
                        LocalDate.ofInstant(subscription.getSubscriptionTime().getStartDate(), ZoneId.of("Asia/Seoul")))
                .endDate(LocalDate.ofInstant(subscription.getSubscriptionTime().getEndDate(), ZoneId.of("Asia/Seoul")))
                .build();
    }

    @Override
    public TerminateSubscriptionResponse terminate(Long userId) {
        Subscription subscription = loadSubscriptionPort.loadByUserId(UserId.make(userId));
        Point point = loadPointByUserUseCase.load(UserId.make(userId));
        Long refundPoint = subscription.refund(userId);
        Long previousPoint = point.getMoney().getTotalPoint();
        point.rechargePoint(userId, refundPoint);
        deleteSubscriptionPort.delete(subscription);
        return new TerminateSubscriptionResponse(userId, previousPoint, refundPoint, point.getMoney().getTotalPoint());
    }

    @Override
    public VerifyUserAccessRightsResponse verify(VerifyUserAccessRightsCommand verifyUserAccessRightsCommand) {
        Subscription subscription = loadSubscriptionPort.loadByUserId(
                UserId.make(verifyUserAccessRightsCommand.userId()));
        Authorization authorization = subscription.verifyAuthentication(verifyUserAccessRightsCommand.userId(),
                verifyUserAccessRightsCommand.type());
        return new VerifyUserAccessRightsResponse(verifyUserAccessRightsCommand.userId(), authorization);
    }

    @Override
    public void renewal(RenewalSubscriptionCommand renewalSubscriptionCommand) {
        Point point = loadPointByUserUseCase.load(UserId.make(renewalSubscriptionCommand.userId()));
        if (existsSubscriptionByUserIdPort.exists(UserId.make(renewalSubscriptionCommand.userId()))) {
            Subscription subscription = loadSubscriptionPort.loadByUserId(UserId.make(renewalSubscriptionCommand.userId()));
            subscription.update(point, renewalSubscriptionCommand.days(), renewalSubscriptionCommand.userId(),
                    renewalSubscriptionCommand.type());
        }
    }
}
