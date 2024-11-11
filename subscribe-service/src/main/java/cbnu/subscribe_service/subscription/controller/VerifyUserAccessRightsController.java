package cbnu.subscribe_service.subscription.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsCommand;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsResponse;
import cbnu.subscribe_service.subscription.service.web.VerifyUserAccessRightsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class VerifyUserAccessRightsController {

    private final VerifyUserAccessRightsUseCase verifyUserAccessRightsUseCase;

    @PostMapping("/verification/rights")
    public BaseResponse<?> verify(@Validated @RequestBody VerifyUserAccessRightsRequest verifyUserAccessRightsRequest) {
        VerifyUserAccessRightsCommand verifyUserAccessRightsCommand = new VerifyUserAccessRightsCommand(
                verifyUserAccessRightsRequest.userId(), verifyUserAccessRightsRequest.type());
        VerifyUserAccessRightsResponse verifyUserAccessRightsResponse = verifyUserAccessRightsUseCase.verify(verifyUserAccessRightsCommand);
        return new BaseResponse<>(200, "OK", verifyUserAccessRightsResponse);
    }
}
