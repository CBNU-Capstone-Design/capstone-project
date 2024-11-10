package cbnu.subscribe_service.point.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.point.service.web.LoadPointResponse;
import cbnu.subscribe_service.point.service.web.PresentPointCommand;
import cbnu.subscribe_service.point.service.web.PresentPointUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class PresentPointController {

    private final PresentPointUseCase presentPointUseCase;

    @PostMapping("/present/point")
    public BaseResponse<?> present(@Validated @RequestBody PresentPointRequest presentPointRequest) {
        PresentPointCommand presentPointCommand = new PresentPointCommand(presentPointRequest.userId(),
                presentPointRequest.toUserId(),
                presentPointRequest.presentPoint());
        LoadPointResponse loadPointResponse = presentPointUseCase.present(presentPointCommand);

        return new BaseResponse<>(200, "OK", loadPointResponse);
    }
}
