package capstone.cycle.refreshtoken.repository;

import capstone.cycle.refreshtoken.entity.RefreshToken;
import capstone.cycle.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM refresh_tokens rt " +
            "WHERE rt.user_id = :userId AND rt.expiry_date > :now)",
            nativeQuery = true)
    boolean existsValidTokenForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    @Modifying
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    Optional<RefreshToken> findByUser(User user);
}
