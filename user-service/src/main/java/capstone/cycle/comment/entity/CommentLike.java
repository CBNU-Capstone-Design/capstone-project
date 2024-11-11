package capstone.cycle.comment.entity;

import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_likes",
        indexes = {
                @Index(name = "idx_comment_user", columnList = "comment_id,user_id", unique = true)
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 정적 팩토리 메서드
    public static CommentLike createCommentLike(Comment comment, User user) {
        return CommentLike.builder()
                .comment(comment)
                .user(user)
                .build();
    }

    // 연관관계 변경을 위한 메서드
    public CommentLike withComment(Comment newComment) {
        return CommentLike.builder()
                .id(this.id)
                .comment(newComment)
                .user(this.user)
                .build();
    }

    public CommentLike withUser(User newUser) {
        return CommentLike.builder()
                .id(this.id)
                .comment(this.comment)
                .user(newUser)
                .build();
    }
}
