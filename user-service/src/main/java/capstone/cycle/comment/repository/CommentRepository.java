package capstone.cycle.comment.repository;

import capstone.cycle.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /*@Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH c.replies r " +
            "LEFT JOIN FETCH r.author ra " +
            "LEFT JOIN FETCH ra.profileImage " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE c.post.id = :postId AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdWithDetailsAndReplies(@Param("postId") Long postId);*/

    // 루트 댓글만 조회
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.author a " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE c.post.id = :postId AND c.parent IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findParentCommentsByPostId(@Param("postId") Long postId);

    // 대댓글 조회
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.author a " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE c.parent.id IN :parentIds " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.author a " +
            "LEFT JOIN FETCH c.likes cl " +
            "LEFT JOIN FETCH cl.user " +
            "WHERE c.id = :commentId AND c.post.id = :postId")
    Optional<Comment> findByIdAndPostIdWithDetails(
            @Param("commentId") Long commentId,
            @Param("postId") Long postId
    );

    @Query("SELECT COUNT(c) > 0 FROM Comment c " +
            "WHERE c.id = :commentId " +
            "AND c.post.id = :postId " +
            "AND c.author.id = :authorId")
    boolean existsByIdAndPostIdAndAuthorId(
            @Param("commentId") Long commentId,
            @Param("postId") Long postId,
            @Param("authorId") Long authorId
    );

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithParent(@Param("commentId") Long commentId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Comment c WHERE c.id = :parentId AND c.parent IS NULL")
    boolean existsByIdAndParentIsNull(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) > 0 FROM Comment c " +
            "WHERE c.id = :commentId AND c.post.id = :postId AND c.parent IS NULL")
    boolean existsByIdAndPostIdAndParentIsNull(
            @Param("commentId") Long commentId,
            @Param("postId") Long postId
    );

    // 게시글 최신 댓글 1개(대댓글 X)
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE  c.post.id = :postId " +
            "AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findLatestCommentByPostId(
            @Param("postId") Long postId,
            Pageable pageable
    );
}