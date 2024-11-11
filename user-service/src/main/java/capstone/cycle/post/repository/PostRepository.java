package capstone.cycle.post.repository;

import capstone.cycle.post.entity.Post;
import capstone.cycle.post.entity.PostCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 단일 게시글 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.id = :id")
    Optional<Post> findById(@Param("id") Long id);

    // 첫 페이지 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findFirstPage(Pageable pageable);

    // 카테고리별 첫 페이지
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.category = :category " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findFirstPageByCategory(
            @Param("category") PostCategory category,
            Pageable pageable);

    // 인기 게시물 첫 페이지
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.likeCount >= :likeThreshold " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC")
    Slice<Post> findFirstPagePopularPosts(
            @Param("likeThreshold") Long likeThreshold,
            Pageable pageable);

    // 무한 스크롤용 다음 페이지 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.createdAt < (SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findAllForInfiniteScroll(
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 카테고리별 무한 스크롤
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.category = :category " +
            "AND p.createdAt < (SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findByCategoryForInfiniteScroll(
            @Param("category") PostCategory category,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 인기 게시물 무한 스크롤
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "WHERE p.likeCount >= :likeThreshold " +
            "AND p.createdAt < (SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC")
    Slice<Post> findPopularPostsForInfiniteScroll(
            @Param("likeThreshold") Long likeThreshold,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 조회수 증가 (벌크 연산)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.club.id = :clubId")
    void deleteAllByClubId(@Param("clubId") Long clubId); // ****** 수정 필요

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.club.id = :clubId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findClubPostsFirstPage(
            @Param("clubId") Long clubId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.club.id = :clubId " +
            "AND p.createdAt < (SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findClubPostsNextPage(
            @Param("clubId") Long clubId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club IS NULL " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findMyPostsFirstPage(
            @Param("userId") Long userId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club IS NULL " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findMyPostsNextPage(
            @Param("userId") Long userId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club.id = :clubId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findMyClubPostsFirstPage(
            @Param("userId") Long userId,
            @Param("clubId") Long clubId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club.id = :clubId " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findMyClubPostsNextPage(
            @Param("userId") Long userId,
            @Param("clubId") Long clubId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club IS NULL " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findUserPostsFirstPage(
            @Param("userId") Long userId,
            Pageable pageable);


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club IS NULL " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findUserPostsNextPage(
            @Param("userId") Long userId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club.id = :clubId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findUserClubPostsFirstPage(
            @Param("userId") Long userId,
            @Param("clubId") Long clubId,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.author.id = :userId " +
            "AND p.club.id = :clubId " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findUserClubPostsNextPage(
            @Param("userId") Long userId,
            @Param("clubId") Long clubId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.club IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> searchPostsFirstPage(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.files " +
            "WHERE p.club IS NULL " +
            "AND p.id < :lastPostId " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> searchPostsNextPage(
            @Param("keyword") String keyword,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

}
