package cbnu.senier.facility_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityRegisterDto {

    private String name;
    private String type;
    private String lat;
    private String lng;
    private String address;
}
