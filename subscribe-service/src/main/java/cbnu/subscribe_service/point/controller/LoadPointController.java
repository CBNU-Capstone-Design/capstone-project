package cbnu.subscribe_service.point.controller;

import cbnu.subscribe_service.common.annotation.WebAdapter;
import cbnu.subscribe_service.common.dto.BaseResponse;
import cbnu.subscribe_service.point.service.web.LoadPointQuery;
import cbnu.subscribe_service.point.service.web.LoadPointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class LoadPointController {

    private final LoadPointQuery loadPointQuery;

    @GetMapping("/load/point/{userId}")
    public BaseResponse<?> load(@PathVariable Long userId) {
        LoadPointResponse loadPointResponse = loadPointQuery.load(userId);
        return new BaseResponse<>(200, "OK", loadPointResponse);
    }
}
