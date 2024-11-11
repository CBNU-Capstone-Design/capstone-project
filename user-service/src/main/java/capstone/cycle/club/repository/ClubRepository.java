package capstone.cycle.club.repository;

import capstone.cycle.club.entity.Club;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "ORDER BY c.createdAt DESC")
    Slice<Club> findAllClubsFirstPage(Pageable pageable);

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE c.activityArea.locality = :city " +
            "ORDER BY c.createdAt DESC")
    Slice<Club> findByCityFirstPage(
            @Param("city") String city,
            Pageable pageable
    );

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE c.id < :lastClubId " +
            "ORDER BY c.createdAt DESC")
    Slice<Club> findAllClubsNextPage(
            @Param("lastClubId") Long lastClubId,
            Pageable pageable
    );

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE c.activityArea.locality = :city " +
            "AND c.id < :lastClubId " +
            "ORDER BY c.createdAt DESC")
    Slice<Club> findByCityNextPage(
            @Param("city") String city,
            @Param("lastClubId") Long lastClubId,
            Pageable pageable
    );

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "JOIN ClubMember cm ON cm.club = c " +
            "WHERE cm.user.id = :userId")
    List<Club> findMyClubs(@Param("userId") Long userId);

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND (:lastClubId IS NULL OR c.id < :lastClubId) " +
            "ORDER BY c.id DESC ")
    Slice<Club> searchClubs(
            @Param("keyword") String keyword,
            @Param("lastClubId") Long lastClubId,
            Pageable pageable
    );

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.leader " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE c.id = :clubId")
    Optional<Club> findByIdWithDetails(
            @Param("clubId") Long clubId
    );

    @Query("SELECT COUNT(c) > 0 FROM Club c WHERE c.name = :name")
    boolean existsByName(@Param("name") String name);

    // 수정 시 자기 자신을 제외한 중복 체크
    @Query("SELECT COUNT(c) > 0 FROM Club c WHERE c.name = :name AND c.id != :clubId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("clubId") Long clubId);

    @Query("SELECT DISTINCT c FROM Club c " +
            "LEFT JOIN c.members m " +
            "LEFT JOIN FETCH c.clubImage " +
            "WHERE m.user.id = :userId")
    List<Club> findClubsByUserId(@Param("userId") Long userId);

}
