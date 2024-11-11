package cbnu.senier.facility_service.service;

import cbnu.senier.facility_service.dto.FacilityRegisterDto;
import cbnu.senier.facility_service.dto.FacilityResponse;
import cbnu.senier.facility_service.entity.Facility;
import cbnu.senier.facility_service.repository.FacilityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService{

    private final FacilityRepository facilityRepository;

    @Override
    public void save(FacilityRegisterDto facilityRegisterDto) {
        Facility facility = Facility.builder()
                .name(facilityRegisterDto.getName())
                .type(facilityRegisterDto.getType())
                .lat(facilityRegisterDto.getLat())
                .lng(facilityRegisterDto.getLng())
                .address(facilityRegisterDto.getAddress())
                .build();

        facilityRepository.save(facility);
    }

    @Override
    public void saveAll(List<FacilityRegisterDto> facilityRegisterDtos) {
        List<Facility> facilities = facilityRegisterDtos.stream()
                .map(facilityRegisterDto -> Facility.builder()
                        .name(facilityRegisterDto.getName())
                        .type(facilityRegisterDto.getType())
                        .lat(facilityRegisterDto.getLat())
                        .lng(facilityRegisterDto.getLng())
                        .address(facilityRegisterDto.getAddress())
                        .build())
                .toList();

        facilityRepository.saveAll(facilities);
    }

    @Override
    public List<FacilityResponse> findAll() {
        return facilityRepository.findAll()
                .stream()
                .map(facility -> {
                    return FacilityResponse.builder()
                            .id(facility.getId())
                            .name(facility.getName())
                            .type(facility.getType())
                            .lat(facility.getLat())
                            .lng(facility.getLng())
                            .address(facility.getAddress())
                            .build();
                })
                .toList();
    }
}
