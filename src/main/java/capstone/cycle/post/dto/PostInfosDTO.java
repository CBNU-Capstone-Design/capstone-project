package capstone.cycle.post.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostInfosDTO {
    private List<SimplePostResponseDTO> content;
}
