package cbnu.senier.facility_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private String lat;

    private String lng;

    private String address;

    @Builder
    public Facility(String address, String lat, String lng, String name, String type) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.type = type;
    }

}
