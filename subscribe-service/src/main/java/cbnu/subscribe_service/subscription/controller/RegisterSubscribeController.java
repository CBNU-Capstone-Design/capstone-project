package cbnu.subscribe_service.subscription.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipCommand;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipResponse;
import cbnu.subscribe_service.subscription.service.web.SubscribeMemberShipUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class RegisterSubscribeController {

    private final SubscribeMemberShipUseCase subscribeMemberShipUseCase;

    @PostMapping("/register/subscription")
    public BaseResponse<?> test(@Validated @RequestBody SubscribeMemberShipRequest subscribeMemberShipRequest) {
        SubscribeMemberShipCommand subscribeMemberShipCommand = getSubscribeMemberShipCommand(
                subscribeMemberShipRequest);
        SubscribeMemberShipResponse subscribeMemberShipResponse = subscribeMemberShipUseCase.subscribe(
                subscribeMemberShipCommand);
        return new BaseResponse<>(200, "OK", subscribeMemberShipResponse);
    }

    private SubscribeMemberShipCommand getSubscribeMemberShipCommand(
            SubscribeMemberShipRequest subscribeMemberShipRequest) {
        return new SubscribeMemberShipCommand(
                subscribeMemberShipRequest.userId(), subscribeMemberShipRequest.type(),
                subscribeMemberShipRequest.days());
    }
}
