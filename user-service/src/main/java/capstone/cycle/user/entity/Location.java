package capstone.cycle.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Location {

    @NotNull
    private String administrativeArea; // 도

    @NotNull
    private String locality; // 건물 번호


    public static Location createDefaultLocation() {
        return new Location(
                "강원도",
                "춘천시"
        );
    }

    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (administrativeArea != null) sb.append(administrativeArea);
        if (locality != null) sb.append(" ").append(locality);
        return sb.toString().trim();
    }
}