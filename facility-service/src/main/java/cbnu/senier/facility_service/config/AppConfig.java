package cbnu.senier.facility_service.config;

import cbnu.senier.facility_service.repository.FacilityRepository;
import cbnu.senier.facility_service.service.FacilityService;
import cbnu.senier.facility_service.service.FacilityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final FacilityRepository facilityRepository;

    @Bean
    public FacilityService facilityService() {
        return new FacilityServiceImpl(facilityRepository);
    }
}
