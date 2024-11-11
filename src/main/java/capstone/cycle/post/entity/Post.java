package capstone.cycle.post.entity;

import capstone.cycle.club.entity.Club;
import capstone.cycle.file.entity.File;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Post {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post")
    private List<File> files = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    public List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    // 일반 게시글 생성
    public static Post createPost(String title, String content, User author, PostCategory category) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .build();
    }

    // 동호회 커뮤니티 게시글 생성
    public static Post createClubPost(String title, String content, User author, Club club) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .club(club)
                .category(PostCategory.CLUB_COMMUNITY)
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .build();
    }


    public Post updateContent(String newTitle, String newContent, PostCategory newCategory) {
        return this.toBuilder()
                .title(newTitle)
                .content(newContent)
                .category(newCategory)
                .build();
    }

    public Post updateClubContent(String newTitle, String newContent) {
        return this.toBuilder()
                .title(newTitle)
                .content(newContent)
                .build();
    }

    // 조회수 증가
    public Post incrementViewCount() {
        return this.toBuilder()
                .viewCount(this.viewCount + 1)
                .build();
    }

    // 좋아요 증가/감소
    public Post incrementLikeCount() {
        return this.toBuilder()
                .likeCount(this.likeCount + 1)
                .build();
    }

    public Post decrementLikeCount() {
        return this.toBuilder()
                .likeCount(Math.max(0, this.likeCount - 1))
                .build();
    }

    // FileGroup 설정
    public Post withContentImageGroup(List<File> files) {
        return this.toBuilder()
                .files(files)
                .build();
    }

    // 댓글 수 증가
    public Post incrementCommentCount() {
        return this.toBuilder()
                .commentCount(this.commentCount + 1)
                .build();
    }

    // 댓글 수 감소
    public Post decrementCommentCount() {
        return this.toBuilder()
                .commentCount(Math.max(0, this.commentCount - 1))
                .build();
    }
}
