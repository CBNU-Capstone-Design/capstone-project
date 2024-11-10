package cbnu.subscribe_service.point.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.point.service.web.RegisterPointCommand;
import cbnu.subscribe_service.point.service.web.RegisterPointUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class RegisterPointController {

    private final RegisterPointUseCase registerPointUseCase;

    @PostMapping("/register/point")
    public BaseResponse<?> register(@Validated @RequestBody RegisterPointRequest registerPointRequest) {
        RegisterPointCommand registerPointCommand = new RegisterPointCommand(registerPointRequest.userId());
        registerPointUseCase.register(registerPointCommand);
        return new BaseResponse<>(200, "OK");
    }
}
