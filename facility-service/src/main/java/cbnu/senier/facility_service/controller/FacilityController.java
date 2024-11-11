package cbnu.senier.facility_service.controller;

import cbnu.senier.facility_service.dto.FacilityRegisterDto;
import cbnu.senier.facility_service.dto.FacilityResponse;
import cbnu.senier.facility_service.service.FacilityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody FacilityRegisterDto facilityRegisterDto) {
        facilityService.save(facilityRegisterDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/all")
    public ResponseEntity<?> registerAll(@RequestBody List<FacilityRegisterDto> facilityRegisterDtos) {
        facilityService.saveAll(facilityRegisterDtos);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FacilityResponse>> find() {
        List<FacilityResponse> result = facilityService.findAll();
        return ResponseEntity.ok(result);
    }
}
