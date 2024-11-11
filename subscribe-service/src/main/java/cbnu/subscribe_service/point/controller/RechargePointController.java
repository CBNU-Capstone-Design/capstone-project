package cbnu.subscribe_service.point.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.point.domain.Money;
import cbnu.subscribe_service.point.service.web.RechargePointCommand;
import cbnu.subscribe_service.point.service.web.RechargePointUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class RechargePointController {

    private final RechargePointUseCase rechargePointUseCase;

    @PutMapping("/recharge/point")
    public BaseResponse<?> recharge(@Validated @RequestBody RechargePointRequest rechargePointRequest) {
        RechargePointCommand rechargePointCommand = new RechargePointCommand(rechargePointRequest.userId(),
                rechargePointRequest.point());
        Money result = rechargePointUseCase.recharge(rechargePointCommand);
        return new BaseResponse<>(200, "OK", result);
    }
}
