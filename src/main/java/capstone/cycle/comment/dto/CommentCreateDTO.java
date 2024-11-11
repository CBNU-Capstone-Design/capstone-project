package capstone.cycle.comment.dto;

import lombok.*;
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommentCreateDTO {
    private String content;
    private Long parentId;

    public static CommentCreateDTO of(String content, Long parentId) {
        return CommentCreateDTO.builder()
                .content(content)
                .parentId(parentId)
                .build();
    }
}