package cbnu.subscribe_service.subscription.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.subscription.service.web.TerminateSubscriptionResponse;
import cbnu.subscribe_service.subscription.service.web.TerminateSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class TerminateSubscriptionController {

    private final TerminateSubscriptionUseCase terminateSubscriptionUseCase;

    @DeleteMapping("/terminate/subscription")
    public BaseResponse<?> terminate(
            @Validated @RequestBody TerminateSubscriptionRequest terminateSubscriptionRequest) {
        TerminateSubscriptionResponse terminateSubscriptionResponse = terminateSubscriptionUseCase.terminate(
                terminateSubscriptionRequest.userId());
        return new BaseResponse<>(200, "OK", terminateSubscriptionResponse);
    }
}
