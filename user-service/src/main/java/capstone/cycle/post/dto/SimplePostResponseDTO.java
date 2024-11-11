package capstone.cycle.post.dto;

import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.post.entity.Post;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePostResponseDTO {
    private Long id;
    private String title;
    private String categoryName;
    private Long viewCount;
    private Long likeCount;
    private LikeStatus likeStatus;
    private String firstImageUrl;
    private String authorName;
    private Long commentCount;

    public static SimplePostResponseDTO fromPost(Post post, LikeStatus likeStatus) {
        return SimplePostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .categoryName(post.getCategory().getDisplayName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .firstImageUrl(getFirstImageUrl(post))
                .authorName(post.getAuthor().getNickname())
                .likeStatus(likeStatus)
                .build();
    }

    private static String getFirstImageUrl(Post post) {
        if (post.getFiles() != null &&
                !post.getFiles().isEmpty()) {
            return post.getFiles().get(0).getPath();
        }
        return null;
    }

}