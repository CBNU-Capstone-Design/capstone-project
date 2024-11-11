package capstone.cycle.user.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class updateLocationDTO {

    private String administrativeArea;
    private String locality;

    public static updateLocationDTO of(String administrativeArea, String locality) {
        return updateLocationDTO.builder()
                .administrativeArea(administrativeArea)
                .locality(locality)
                .build();
    }
}
