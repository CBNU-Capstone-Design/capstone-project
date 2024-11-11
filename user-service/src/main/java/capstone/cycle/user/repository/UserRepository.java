package capstone.cycle.user.repository;

import capstone.cycle.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 소셜 로그인/회원가입용 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.socialId = :socialId AND u.socialProvider = :socialProvider")
    Optional<User> findBySocialIdAndSocialProvider(
            @Param("socialId") String socialId,
            @Param("socialProvider") String socialProvider
    );

    // 단일 사용자 프로필 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    // 관리자용 전체 사용자 조회
    @Query("SELECT u FROM User u " +
            "ORDER BY u.createdAt DESC")
    List<User> findAllWithProfile();

    // 닉네임으로 사용자 조회 (중복 체크용)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.nickname = :nickname")
    boolean existsByNickname(@Param("nickname") String nickname);

    /*// 리프레시 토큰으로 사용자 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.refreshToken = :refreshToken")
    Optional<User> findByRefreshToken(@Param("refreshToken") String refreshToken);*/

    // socialId로 사용자 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.socialId = :socialId")
    Optional<User> findBySocialId(@Param("socialId") String socialId);

    // 닉네임으로 사용자 검색 (부분 일치)
    @Query("SELECT u FROM User u " +
            "WHERE u.nickname LIKE %:keyword% " +
            "ORDER BY u.nickname ASC")
    List<User> searchByNickname(@Param("keyword") String keyword);

    // 사용자 역할 조회
    @Query("SELECT u.role FROM User u WHERE u.id = :id")
    Optional<String> findRoleById(@Param("id") Long id);

    /*// 주소 정보만 조회
    @Query("SELECT new map(u.workAddress as workAddress, u.homeAddress as homeAddress) " +
            "FROM User u WHERE u.id = :id")
    Optional<Map<String, Location>> findAddressesById(@Param("id") Long id);*/
}