package capstone.cycle.post.dto;

import capstone.cycle.comment.entity.Comment;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPreviewResponse {
    private Long id;
    private String content;
    private SimpleUserInfoDTO author;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean likedByCurrentUser;

    public static CommentPreviewResponse empty() {
        return CommentPreviewResponse.builder()
                .id(null)
                .content("")
                .author(null)
                .createdAt(null)
                .likeCount(0)
                .likedByCurrentUser(false)
                .build();
    }

    // 댓글이 있는지 확인
    public boolean exists() {
        return id != null;
    }

    public static CommentPreviewResponse from(Comment comment, Long currentUserId) {

        if (comment == null) {
            return empty();
        }

        return CommentPreviewResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(SimpleUserInfoDTO.from(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount())
                .likedByCurrentUser(comment.getLikes().stream()
                        .anyMatch(like -> like.getUser().getId().equals(currentUserId)))
                .build();
    }
}
