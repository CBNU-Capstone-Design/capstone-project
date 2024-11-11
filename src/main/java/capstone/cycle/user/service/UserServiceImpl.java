package capstone.cycle.user.service;

import capstone.cycle.club.dto.ClubSummaryDTO;
import capstone.cycle.club.entity.Club;
import capstone.cycle.club.repository.ClubRepository;
import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.user.dto.*;
import capstone.cycle.user.entity.Location;
import capstone.cycle.user.entity.User;
import capstone.cycle.common.security.role.UserRole;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public UserDTO socialLogin(SocialLoginDTO socialLoginDTO) {
        User user = userRepository.findBySocialIdAndSocialProvider(
                socialLoginDTO.getSocialId(),
                socialLoginDTO.getSocialProvider()
        ).orElseGet(() -> createNewUser(socialLoginDTO));

        log.info("User logged in successfully: {}", user.getId());
        return user.toDTO();
    }

    @Override
    @Transactional
    public void logout(String accessToken, Long userId) {
        try {
            redisTokenService.blacklistAndInvalidateTokens(accessToken, userId);
        } catch (Exception e) {
            log.error("Error during logout process for user: {}", userId, e);
            throw new TokenException(TokenErrorResult.LOGOUT_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DetailUserInfoDTO getUserDetailInfo(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
        return DetailUserInfoDTO.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfosDTO getAllUserInfos(Long id) {
        String role = userRepository.findRoleById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        if (!"ROLE_ADMIN".equals(role)) {
            throw new AccessDeniedException("관리자만 접근 가능합니다.");
        }

        List<SimpleUserInfoDTO> userInfos = userRepository.findAllWithProfile().stream()
                .map(SimpleUserInfoDTO::from)
                .collect(Collectors.toList());

        return new UserInfosDTO(userInfos);
    }

    @Override
    @Transactional
    public void updateNickname(String nickname, Long id) {
        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(UserErrorResult.ALREADY_USED_NICKNAME);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        User updatedUser = user.withNickname(nickname);
        userRepository.save(updatedUser);
        log.info("Updated nickname for user: {}", id);
    }

    @Override
    @Transactional
    public void updateLocation(updateLocationDTO updateLocationDTO, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Location currentLocation = new Location(
                updateLocationDTO.getAdministrativeArea(),
                updateLocationDTO.getLocality()
        );

        User updatedUser = user.withCurrentLocation(currentLocation);
        userRepository.save(updatedUser);
        log.info("Updated location for user: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long targetUserId, Long currentUserId) {
        DetailUserInfoDTO userInfo = getUserDetailInfo(targetUserId);

        List<Club> userClubs = clubRepository.findClubsByUserId(targetUserId);
        List<ClubSummaryDTO> clubDTOs = userClubs.stream()
                .map(ClubSummaryDTO::of)
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .userInfo(userInfo)
                .clubs(clubDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        DetailUserInfoDTO myInfo = getUserDetailInfo(userId);

        List<Club> myClubs = clubRepository.findMyClubs(userId);
        List<ClubSummaryDTO> myClubDTOs = myClubs.stream()
                .map(ClubSummaryDTO::of)
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .userInfo(myInfo)
                .clubs(myClubDTOs)
                .build();
    }

    private User createNewUser(SocialLoginDTO dto) {
        User newUser = User.createUser(
                dto.getSocialId(),
                dto.getSocialProvider(),
                dto.getEmail(),
                dto.getNickname(),
                UserRole.USER.getRole(),
                dto.getSnsProfileImageUrl(),
                dto.getAdministrativeArea(),
                dto.getLocality()
        );

        User savedUser = userRepository.save(newUser);
        log.info("Created new user: {}", savedUser.getId());
        return savedUser;
    }
}