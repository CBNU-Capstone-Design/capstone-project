package capstone.cycle.like.entity;

import capstone.cycle.post.entity.Post;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        indexes = {
                @Index(name = "idx_post_user", columnList = "post_id,user_id", unique = true),
                @Index(name = "idx_post", columnList = "post_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
            foreignKey = @ForeignKey(
                    name = "fk_like_post",
                    foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE"
            ))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static Like createLike(Post post, User user) {
        return Like.builder()
                .post(post)
                .user(user)
                .build();
    }

    public Like withPost(Post newPost) {
        return Like.builder()
                .id(this.id)
                .post(newPost)
                .user(this.user)
                .createdAt(this.createdAt)
                .build();
    }

    public Like withUser(User newUser) {
        return Like.builder()
                .id(this.id)
                .post(this.post)
                .user(newUser)
                .createdAt(this.createdAt)
                .build();
    }
}
