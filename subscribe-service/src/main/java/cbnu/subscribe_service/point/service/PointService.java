package cbnu.subscribe_service.point.service;

import cbnu.subscribe_service.common.annotation.UseCase;
import cbnu.subscribe_service.domain.UserId;
import cbnu.subscribe_service.point.domain.Money;
import cbnu.subscribe_service.point.domain.Point;
import cbnu.subscribe_service.point.domain.PointId;
import cbnu.subscribe_service.point.service.persistence.ExistsPointByUserIdPort;
import cbnu.subscribe_service.point.service.persistence.LoadPointByUserIdPort;
import cbnu.subscribe_service.point.service.persistence.LoadPointPort;
import cbnu.subscribe_service.point.service.persistence.RegisterPointPort;
import cbnu.subscribe_service.point.service.web.LoadPointByUserUseCase;
import cbnu.subscribe_service.point.service.web.LoadPointQuery;
import cbnu.subscribe_service.point.service.web.LoadPointResponse;
import cbnu.subscribe_service.point.service.web.PresentPointCommand;
import cbnu.subscribe_service.point.service.web.PresentPointUseCase;
import cbnu.subscribe_service.point.service.web.RechargePointCommand;
import cbnu.subscribe_service.point.service.web.RechargePointUseCase;
import cbnu.subscribe_service.point.service.web.RegisterPointCommand;
import cbnu.subscribe_service.point.service.web.RegisterPointUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
@RequiredArgsConstructor
class PointService implements RegisterPointUseCase, RechargePointUseCase, LoadPointByUserUseCase, LoadPointQuery,
        PresentPointUseCase {

    private final RegisterPointPort registerPointPort;
    private final LoadPointByUserIdPort loadPointByUserIdPort;
    private final ExistsPointByUserIdPort existsPointByUserIdPort;

    @Override
    public void register(RegisterPointCommand registerPointCommand) {
        if (!existsPointByUserIdPort.exist(UserId.make(registerPointCommand.userId()))) {
            Point point = Point.make(registerPointCommand.userId());
            registerPointPort.register(point);
        }
    }

    @Override
    public Money recharge(RechargePointCommand rechargePointCommand) {
        Point point = loadPointByUserIdPort.loadByUserId(UserId.make(rechargePointCommand.userId()));
        point.rechargePoint(rechargePointCommand.userId(), rechargePointCommand.point());
        return point.getMoney();
    }

    @Override
    public Point load(UserId userId) {
        return loadPointByUserIdPort.loadByUserId(userId);
    }

    @Override
    public LoadPointResponse load(Long userId) {
        Point point = loadPointByUserIdPort.loadByUserId(UserId.make(userId));
        return new LoadPointResponse(point.getUserId().getId(),point.getMoney().getTotalPoint());
    }

    @Override
    public LoadPointResponse present(PresentPointCommand presentPointCommand) {
        Point userPoint = loadPointByUserIdPort.loadByUserId(UserId.make(presentPointCommand.userId()));
        Point opponentPoint = loadPointByUserIdPort.loadByUserId(UserId.make(presentPointCommand.toUserId()));
        Long currentPoint = userPoint.presentPoint(opponentPoint, presentPointCommand.sendPoint());
        return new LoadPointResponse(presentPointCommand.userId(), currentPoint);
    }
}
