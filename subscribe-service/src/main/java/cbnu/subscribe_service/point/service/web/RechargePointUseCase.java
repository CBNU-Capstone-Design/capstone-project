package cbnu.subscribe_service.point.service.web;

import cbnu.subscribe_service.point.domain.Money;

public interface RechargePointUseCase {
    Money recharge(RechargePointCommand rechargePointCommand);
}
