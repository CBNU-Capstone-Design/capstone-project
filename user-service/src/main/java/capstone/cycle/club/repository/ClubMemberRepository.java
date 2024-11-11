package capstone.cycle.club.repository;

import capstone.cycle.club.entity.member.ClubMember;
import capstone.cycle.club.entity.member.ClubMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {

    @Query("SELECT COUNT(cm) FROM ClubMember cm WHERE cm.user.id =:userId")
    int countByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END FROM ClubMember cm " +
            "WHERE cm.club.id = :clubId AND cm.user.id = :userId")
    boolean existsByClubIdAndUserId(
            @Param("clubId") Long clubId,
            @Param("userId") Long userId
    );

    @Modifying
    @Query("DELETE FROM ClubMember cm WHERE cm.club.id = :clubId")
    void deleteAllByClubId(@Param("clubId") Long clubId);

    @Query("SELECT cm FROM ClubMember cm " +
            "WHERE cm.club.id = :clubId AND cm.user.id = :userId")
    Optional<ClubMember> findByClubIdAndUserId(
            @Param("clubId") Long clubId,
            @Param("userId") Long userId
    );
}
