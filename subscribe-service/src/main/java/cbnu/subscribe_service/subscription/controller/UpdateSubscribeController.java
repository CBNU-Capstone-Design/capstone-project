package cbnu.subscribe_service.subscription.controller;

import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.subscription.service.web.RenewalSubscriptionCommand;
import cbnu.subscribe_service.subscription.service.web.RenewalSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class UpdateSubscribeController {

    private final RenewalSubscriptionUseCase renewalSubscriptionUseCase;

    @PutMapping("/renewal/subscription")
    public BaseResponse<?> renewal(@Validated @RequestBody RenewalSubscriptionRequest renewalSubscriptionRequest) {
        RenewalSubscriptionCommand renewalSubscriptionCommand = new RenewalSubscriptionCommand(
                renewalSubscriptionRequest.userId(), renewalSubscriptionRequest.type(),
                renewalSubscriptionRequest.days());
        renewalSubscriptionUseCase.renewal(renewalSubscriptionCommand);
        return new BaseResponse<>(200, "OK");
    } 
}
