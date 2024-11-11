package capstone.cycle.post.dto;

import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.post.entity.Post;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostDetailResponse {

    private PostResponseDTO post;
    private List<CommentResponseDTO> comments;

    public static PostDetailResponse of(Post post, List<CommentResponseDTO> comments, LikeStatus likeStatus) {
        return PostDetailResponse.builder()
                .post(new PostResponseDTO(post, likeStatus))
                .comments(comments)
                .build();
    }
}
