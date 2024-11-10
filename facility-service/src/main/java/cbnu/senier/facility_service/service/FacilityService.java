package cbnu.senier.facility_service.service;

import cbnu.senier.facility_service.dto.FacilityRegisterDto;
import cbnu.senier.facility_service.dto.FacilityResponse;
import java.util.List;

public interface FacilityService {

    void save(FacilityRegisterDto facilityRegisterDto);

    void saveAll(List<FacilityRegisterDto> facilityRegisterDtos);

    List<FacilityResponse> findAll();
}
