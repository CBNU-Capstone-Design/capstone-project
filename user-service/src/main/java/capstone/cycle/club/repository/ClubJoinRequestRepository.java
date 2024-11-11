package capstone.cycle.club.repository;

import capstone.cycle.club.entity.request.ClubJoinRequest;
import capstone.cycle.club.entity.request.ClubJoinRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, ClubJoinRequestId> {
    @Query("SELECT COUNT(r) > 0 FROM ClubJoinRequest r " +
            "WHERE r.club.id = :clubId " +
            "AND r.user.id = :userId " +
            "AND r.status = 'PENDING'")
    boolean existsPendingRequest(
            @Param("clubId") Long clubId,
            @Param("userId") Long userId
    );

    @Query("SELECT r FROM ClubJoinRequest r " +
            "LEFT JOIN FETCH r.user " +
            "WHERE r.club.id = :clubId " +
            "AND r.user.id = :userId " +
            "AND r.status = 'PENDING'")
    Optional<ClubJoinRequest> findByClubIdAndUserId(
            @Param("clubId") Long clubId,
            @Param("userId") Long userId
    );

    @Query("SELECT r FROM ClubJoinRequest r " +
            "LEFT JOIN FETCH r.user " +
            "WHERE r.club.id = :clubId " +
            "AND r.status = 'PENDING' " +
            "ORDER BY r.createdAt DESC")
    List<ClubJoinRequest> findByClubId(
            @Param("clubId") Long clubId
    );

    @Modifying
    @Query("DELETE FROM ClubJoinRequest r WHERE r.club.id = :clubId")
    void deleteAllByClubId(@Param("clubId") Long clubId);

    @Query("SELECT r FROM ClubJoinRequest  r " +
            "JOIN FETCH r.club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE r.user.id = :userId " +
            "AND r.status = 'PENDING' " +
            "ORDER BY r.createdAt DESC")
    List<ClubJoinRequest> findMyRequests(@Param("userId") Long userId);

}
