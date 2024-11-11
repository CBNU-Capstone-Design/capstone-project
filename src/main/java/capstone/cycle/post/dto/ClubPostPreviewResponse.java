package capstone.cycle.post.dto;

import capstone.cycle.club.dto.ClubMemberInfoResponse;
import capstone.cycle.comment.entity.Comment;
import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.post.entity.Post;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClubPostPreviewResponse {

    private Long id;
    private String title;
    private String content;
    private SimpleUserInfoDTO author;
    private List<ImageResponse> imageUrls;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LikeStatus likeStatus;
    private CommentPreviewResponse latestComment; // 최신 댓글 1개

    public static ClubPostPreviewResponse from(Post post, Comment latestComment, Long currentUserId, LikeStatus status) {
        return ClubPostPreviewResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(SimpleUserInfoDTO.from(post.getAuthor()))
                .imageUrls(post.getFiles().stream()
                        .map(file -> new ImageResponse(file.getId(), file.getPath()))
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likeStatus(status)
                .latestComment(latestComment != null ?
                        CommentPreviewResponse.from(latestComment, currentUserId) :
                        CommentPreviewResponse.empty()) // 빈 객체 반환
                .build();
    }
}
