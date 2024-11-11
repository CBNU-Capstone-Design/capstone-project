package capstone.cycle.comment.repository;

import capstone.cycle.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//사용 X
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("SELECT cl FROM CommentLike cl " +
            "WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    Optional<CommentLike> findByCommentIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );

    // 실제 좋아요 여부만 필요할 때는 exists 쿼리 사용
    @Query("SELECT COUNT(cl) > 0 FROM CommentLike cl " +
            "WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    boolean existsByCommentIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );

    // 특정 댓글의 좋아요 수 조회
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    int countByCommentId(@Param("commentId") Long commentId);

    // 특정 댓글의 모든 좋아요 조회
    @Query("SELECT cl FROM CommentLike cl " +
            "LEFT JOIN FETCH cl.user u " +
            "WHERE cl.comment.id = :commentId")
    List<CommentLike> findAllByCommentIdWithUser(@Param("commentId") Long commentId);

    // 좋아요 삭제 (벌크 연산)
    @Modifying
    @Query("DELETE FROM CommentLike cl " +
            "WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    void deleteByCommentIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );
}
