package capstone.cycle.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfosDTO {
    private List<SimpleUserInfoDTO> content;
}
