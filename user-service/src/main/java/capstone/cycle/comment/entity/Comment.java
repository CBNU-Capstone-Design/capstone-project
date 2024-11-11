package capstone.cycle.comment.entity;

import capstone.cycle.post.entity.Post;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> likes = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드(일반 댓글 생성)
    public static Comment createComment(String content, Post post, User author) {
        return Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .likes(new ArrayList<>())  // likes 리스트 초기화 추가
                .replies(new ArrayList<>()) // replies 리스트 초기화 추가
                .build();
    }

    // 대댓글 생성
    public static Comment createReply(String content, Post post, User author, Comment parent) {
        return Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .parent(parent)
                .likes(new ArrayList<>())
                .replies(new ArrayList<>())
                .build();
    }

    // 컨텐츠 업데이트를 위한 메서드
    public Comment updateContent(String newContent) {
        return Comment.builder()
                .id(this.id)
                .content(newContent)
                .post(this.post)
                .author(this.author)
                .parent(this.parent)
                .replies(this.replies)
                .likes(this.likes)
                .createdAt(this.createdAt)
                .build();
    }

    // 불변 리스트 반환
    public List<Comment> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public List<CommentLike> getLikes() {
        return Collections.unmodifiableList(likes);
    }

    // 댓글 추가
    public Comment addReply(Comment reply) {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot add reply to a reply");
        }
        List<Comment> newReplies = new ArrayList<>(this.replies);
        newReplies.add(reply);

        return Comment.builder()
                .id(this.id)
                .content(this.content)
                .post(this.post)
                .author(this.author)
                .parent(this.parent)
                .replies(newReplies)
                .likes(this.likes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 좋아요 추가
    public Comment addLike(CommentLike like) {
        List<CommentLike> newLikes = new ArrayList<>(this.likes);
        newLikes.add(like);

        return Comment.builder()
                .id(this.id)
                .content(this.content)
                .post(this.post)
                .author(this.author)
                .parent(this.parent)
                .replies(this.replies)
                .likes(newLikes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 좋아요 제거
    public Comment removeLike(CommentLike like) {
        List<CommentLike> newLikes = new ArrayList<>(this.likes);
        newLikes.remove(like);

        return Comment.builder()
                .id(this.id)
                .content(this.content)
                .post(this.post)
                .author(this.author)
                .parent(this.parent)
                .replies(this.replies)
                .likes(newLikes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public int getLikeCount() {
        return this.likes.size();
    }

    public boolean isReply() {
        return this.parent != null;
    }

}
