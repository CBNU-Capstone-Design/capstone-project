package capstone.cycle.comment.dto;

import capstone.cycle.comment.entity.Comment;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {

    private Long id;
    private String content;
    private SimpleUserInfoDTO author;
    private Long parentId;
    private List<CommentResponseDTO> replies;
    private int likeCount;
    private boolean likedByCurrentUser;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public CommentResponseDTO(Comment comment, Long currentUserId) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = SimpleUserInfoDTO.from(comment.getAuthor());
        this.likeCount = comment.getLikeCount();
        this.likedByCurrentUser = comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(currentUserId));
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();

        if (comment.getParent() != null) {
            // 대댓글인 경우
            this.parentId = comment.getParent().getId();
            this.replies = null;
        } else {
            // 원본 댓글인 경우
            this.parentId = null;
            this.replies = List.of();  // 빈 리스트로 초기화, Service에서 채워질 예정
        }
    }

    // 정적 팩토리 메서드
    public static CommentResponseDTO from(Comment comment, Long currentUserId) {
        return new CommentResponseDTO(comment, currentUserId);
    }
}
