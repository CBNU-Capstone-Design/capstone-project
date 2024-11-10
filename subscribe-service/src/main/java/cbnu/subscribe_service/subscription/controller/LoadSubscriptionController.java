package cbnu.subscribe_service.subscription.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.subscription.service.web.LoadSubscriptionQuery;
import cbnu.subscribe_service.subscription.service.web.LoadSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class LoadSubscriptionController {

    private final LoadSubscriptionQuery loadSubscriptionQuery;

    @GetMapping("/load/subscription/{userId}")
    public BaseResponse<?> load(@PathVariable Long userId) {
        LoadSubscriptionResponse loadSubscriptionResponse = loadSubscriptionQuery.load(userId);
        return new BaseResponse<>(200, "OK", loadSubscriptionResponse);
    }
}
