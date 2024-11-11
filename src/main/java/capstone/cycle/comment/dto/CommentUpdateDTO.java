package capstone.cycle.comment.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommentUpdateDTO {
    private String content;

    public static CommentUpdateDTO of(String content) {
        return CommentUpdateDTO.builder()
                .content(content)
                .build();
    }
}
