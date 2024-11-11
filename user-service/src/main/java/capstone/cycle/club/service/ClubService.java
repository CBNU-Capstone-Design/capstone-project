package capstone.cycle.club.service;

import capstone.cycle.club.dto.*;
import capstone.cycle.club.dto.request.JoinRequestCreateDTO;
import capstone.cycle.club.dto.request.JoinRequestListResponseDTO;
import capstone.cycle.club.dto.request.MyClubRequestDTO;
import capstone.cycle.club.entity.*;
import capstone.cycle.club.entity.member.ClubMember;
import capstone.cycle.club.entity.member.ClubMemberRole;
import capstone.cycle.club.entity.request.ClubJoinRequest;
import capstone.cycle.club.entity.request.JoinRequestStatus;
import capstone.cycle.club.error.ClubErrorResult;
import capstone.cycle.club.error.ClubException;
import capstone.cycle.club.repository.ClubJoinRequestRepository;
import capstone.cycle.club.repository.ClubMemberRepository;
import capstone.cycle.club.repository.ClubRepository;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.service.FileService;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.Location;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final PostRepository postRepository;
    private static final int MAX_CLUB_MEMBERSHIP = 3;
    private static final long POPULAR_POST_LIKE_THRESHOLD = 10;


    // =========== 서비스 메서드 ===========
    @Transactional
    public Long createClub(ClubCreateRequest request, MultipartFile clubImage, Long userId) {
        validateClubName(request.getName());
        validateMembershipLimit(userId);

        User leader = findUserById(userId);

        File imageFile = null;
        if (clubImage != null && !clubImage.isEmpty()) {
            imageFile = fileService.uploadFile(null, clubImage);
        }

        Club club = Club.createClub(
                request.getName(),
                request.getDescription(),
                leader,
                request.getMaxMemberCount(),
                new Location(request.getAdministrationArea(), request.getCity())
        );

        if (imageFile != null) {
            club = club.withClubImage(imageFile);
        }

        Club savedClub = clubRepository.save(club);
        ClubMember leaderMember = ClubMember.createMember(savedClub, leader, ClubMemberRole.LEADER);
        clubMemberRepository.save(leaderMember);

        return savedClub.getId();
    }

    @Transactional
    public ClubDetailResponseDTO updateClub(Long clubId, ClubUpdateRequest request, MultipartFile newClubImage, Long userId) {
        Club club = findClubWithDetails(clubId);
        validateLeaderAuthority(club, userId);
        validateClubNameForUpdate(request.getName(), clubId);

        File imageFile = handleClubImage(club, newClubImage);

        Club updatedClub = club.update(
                request.getName(),
                request.getDescription(),
                request.getMaxMemberCount(),
                new Location(request.getAdministrationArea(), request.getCity()),
                imageFile
        );

        Club savedClub = clubRepository.save(updatedClub);
        return ClubDetailResponseDTO.from(savedClub);
    }

    private File handleClubImage(Club club, MultipartFile newImage) {
        if (newImage != null) {
            if (club.getClubImage() != null) {
                fileService.deleteFile(club.getClubImage().getId());
            }
            return fileService.uploadFile(null, newImage);
        }
        return club.getClubImage();  // 새 이미지가 없으면 기존 이미지 유지
    }

    @Transactional
    public void deleteClub(Long clubId, Long userId) {
        Club club = findClubWithDetails(clubId);
        validateLeaderAuthority(club, userId);

        // 클럽 이미지 삭제
        if (club.getClubImage() != null) {
            fileService.deleteFile(club.getClubImage().getId());
        }

        // 클럽 내 게시글과 관련된 데이터 삭제
        postRepository.deleteAllByClubId(clubId);  // 게시글과 연관된 댓글, 좋아요도 CASCADE로 삭제

        // 클럽 멤버십 관련 데이터 삭제
        clubMemberRepository.deleteAllByClubId(clubId);
        joinRequestRepository.deleteAllByClubId(clubId);

        clubRepository.delete(club);
    }

    @Transactional
    public void expelMember(Long clubId, Long targetUserId, Long requesterId) {
        Club club = findClubById(clubId);
        ClubMember requester = findClubMember(clubId, requesterId);

        validateManagerAuthority(requester);

        if (club.getLeader().getId().equals(targetUserId)) {
            throw new ClubException(ClubErrorResult.CANNOT_EXPEL_LEADER);
        }

        ClubMember targetMember = findClubMember(clubId, targetUserId);
        clubMemberRepository.delete(targetMember);

        Club updatedClub = club.decrementMemberCount();
        clubRepository.save(updatedClub);
    }

    @Transactional
    public void transferLeadership(Long clubId, Long newLeaderId, Long currentLeaderId) {
        Club club = findClubById(clubId);
        validateLeaderAuthority(club, currentLeaderId);

        ClubMember newLeaderMember = findClubMember(clubId, newLeaderId);
        ClubMember currentLeaderMember = findClubMember(clubId, currentLeaderId);

        Club updatedClub = club.withLeader(newLeaderMember.getUser());

        clubMemberRepository.save(currentLeaderMember.updateRole(ClubMemberRole.MEMBER));
        clubMemberRepository.save(newLeaderMember.updateRole(ClubMemberRole.LEADER));
        clubRepository.save(updatedClub);
    }

    @Transactional
    public void assignManager(Long clubId, Long targetUserId, Long requesterId) {
        Club club = findClubById(clubId);

        // 동호회장만 운영진 지정 가능
        validateLeaderAuthority(club, requesterId);

        ClubMember targetMember = findClubMember(clubId, targetUserId);

        // 이미 운영진이면 에러
        if (targetMember.getRole() == ClubMemberRole.MANAGER) {
            throw new ClubException(ClubErrorResult.ALREADY_MANAGER);
        }

        // 동호회장은 운영진으로 지정 불가
        if (targetMember.getRole() == ClubMemberRole.LEADER) {
            throw new ClubException(ClubErrorResult.CANNOT_CHANGE_LEADER_ROLE);
        }

        clubMemberRepository.save(targetMember.updateRole(ClubMemberRole.MANAGER));
    }

    @Transactional
    public void removeManager(Long clubId, Long targetUserId, Long requesterId) {
        Club club = findClubById(clubId);

        // 동호회장만 운영진 해제 가능
        validateLeaderAuthority(club, requesterId);

        ClubMember targetMember = findClubMember(clubId, targetUserId);

        // 운영진이 아니면 에러
        if (targetMember.getRole() != ClubMemberRole.MANAGER) {
            throw new ClubException(ClubErrorResult.NOT_MANAGER);
        }

        clubMemberRepository.save(targetMember.updateRole(ClubMemberRole.MEMBER));
    }

    @Transactional
    public void requestJoin(Long clubId, JoinRequestCreateDTO request, Long userId) {
        Club club = findClubById(clubId);

        validateMembershipLimit(userId);

        if (club.isFull()) {
            throw new ClubException(ClubErrorResult.CLUB_FULL);
        }

        if (clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw new ClubException(ClubErrorResult.ALREADY_MEMBER);
        }

        if (joinRequestRepository.existsPendingRequest(clubId, userId)) {
            throw new ClubException(ClubErrorResult.ALREADY_REQUESTED);
        }

        User user = findUserById(userId);
        ClubJoinRequest joinRequest = ClubJoinRequest.createRequest(club, user, request.getMessage());
        joinRequestRepository.save(joinRequest);
    }

    @Transactional
    public void cancelJoinRequest(Long clubId, Long userId) {
        ClubJoinRequest request = joinRequestRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new ClubException(ClubErrorResult.REQUEST_NOT_FOUND));

        // 대기 상태인 요청만 취소 가능
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new ClubException(ClubErrorResult.INVALID_REQUEST_STATUS);
        }

        joinRequestRepository.delete(request);
    }



    @Transactional
    public void acceptJoinRequest(Long clubId, Long userId, Long leaderId) {
        Club club = findClubById(clubId);
        validateLeaderAuthority(club, leaderId);

        ClubJoinRequest request = findJoinRequest(clubId, userId);
        validateJoinRequestStatus(request);

        ClubMember newMember = ClubMember.createMember(club, request.getUser(), ClubMemberRole.MEMBER);
        clubMemberRepository.save(newMember);

        Club updatedClub = club.incrementMemberCount();
        clubRepository.save(updatedClub);

        ClubJoinRequest acceptedRequest = request.accept();
        joinRequestRepository.save(acceptedRequest);
    }

    @Transactional
    public void rejectJoinRequest(Long clubId, Long requestUserId, Long leaderId) {
        Club club = findClubById(clubId);
        validateLeaderAuthority(club, leaderId);

        ClubJoinRequest request = findJoinRequest(clubId, requestUserId);
        validateJoinRequestStatus(request);

        ClubJoinRequest rejectedRequest = request.reject();
        joinRequestRepository.save(rejectedRequest);
    }

    @Transactional
    public void leaveClub(Long clubId, Long userId) {
        Club club = findClubById(clubId);
        ClubMember member = findClubMember(clubId, userId);

        if (member.getRole() == ClubMemberRole.LEADER) {
            throw new ClubException(ClubErrorResult.LEADER_CANNOT_LEAVE);
        }

        clubMemberRepository.delete(member);

        Club updatedClub = club.decrementMemberCount();
        clubRepository.save(updatedClub);
    }

    // =========== 조회 메서드 ===========
    public ClubDetailResponseDTO getClubDetail(Long clubId) {
        Club club = findClubWithDetails(clubId);
        return ClubDetailResponseDTO.from(club);
    }

    public Slice<ClubListResponseDTO> getClubsByLocation(String city, Long lastClubId, int size) {
        validateCity(city);

        Pageable pageable = PageRequest.of(0, size);
        Slice<Club> clubs = fetchClubsByLocation(city, lastClubId, pageable);

        return clubs.map(ClubListResponseDTO::from);
    }

    private Slice<Club> fetchClubsByLocation(String city, Long lastClubId, Pageable pageable) {
        if (lastClubId == null) {
            return "전국".equals(city)
                    ? clubRepository.findAllClubsFirstPage(pageable)
                    : clubRepository.findByCityFirstPage(city, pageable);
        } else {
            return "전국".equals(city)
                    ? clubRepository.findAllClubsNextPage(lastClubId, pageable)
                    : clubRepository.findByCityNextPage(city, lastClubId, pageable);
        }
    }

    public List<ClubSummaryDTO> getMyClubs(Long userId) {
        List<Club> myClubs = clubRepository.findMyClubs(userId);
        return myClubs.stream()
                .map(ClubSummaryDTO::of)
                .collect(Collectors.toList());
    }

    public List<JoinRequestListResponseDTO> getClubJoinRequests(Long clubId, Long leaderId) {
        Club club = findClubById(clubId);
        validateLeaderAuthority(club, leaderId);

        List<ClubJoinRequest> requests = joinRequestRepository.findByClubId(clubId);
        return requests.stream()
                .map(JoinRequestListResponseDTO::from)
                .collect(Collectors.toList());
    }

    public List<MyClubRequestDTO> getMyRequests(Long userId) {
        List<ClubJoinRequest> requests = joinRequestRepository.findMyRequests(userId);
        return requests.stream()
                .map(MyClubRequestDTO::from)
                .collect(Collectors.toList());
    }

    public Slice<ClubListResponseDTO> searchClubs(String keyword, Long lastClubId, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ClubException(ClubErrorResult.INVALID_SEARCH_KEYWORD);
        }

        Pageable pageable = PageRequest.of(0, size);
        return clubRepository.searchClubs(keyword.trim(), lastClubId, pageable)
                .map(ClubListResponseDTO::from);
    }

    // =========== 공통 조회 메서드 ===========
    public Club findClubById(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ClubErrorResult.CLUB_NOT_FOUND));
    }

    private Club findClubWithDetails(Long clubId) {
        return clubRepository.findByIdWithDetails(clubId)
                .orElseThrow(() -> new ClubException(ClubErrorResult.CLUB_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
    }

    private ClubMember findClubMember(Long clubId, Long userId) {
        return clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new ClubException(ClubErrorResult.NOT_CLUB_MEMBER));
    }

    private ClubJoinRequest findJoinRequest(Long clubId, Long userId) {
        return joinRequestRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new ClubException(ClubErrorResult.REQUEST_NOT_FOUND));
    }

    // =========== 권한 체크 메서드 ===========
    private void validateLeaderAuthority(Club club, Long userId) {
        if (!club.getLeader().getId().equals(userId)) {
            throw new ClubException(ClubErrorResult.UNAUTHORIZED_ACTION);
        }
    }

    private void validateManagerAuthority(ClubMember member) {
        if (member.getRole() != ClubMemberRole.LEADER &&
                member.getRole() != ClubMemberRole.MANAGER) {
            throw new ClubException(ClubErrorResult.UNAUTHORIZED_ACTION);
        }
    }

    // =========== 유효성 검증 메서드 ===========
    private void validateMembershipLimit(Long userId) {
        int currentClubCount = clubMemberRepository.countByUserId(userId);
        if (currentClubCount >= MAX_CLUB_MEMBERSHIP) {
            throw new ClubException(ClubErrorResult.MAX_CLUB_COUNT_EXCEEDED);
        }
    }

    private void validateClubName(String name) {
        if (clubRepository.existsByName(name)) {
            throw new ClubException(ClubErrorResult.CLUB_NAME_ALREADY_EXISTS);
        }
    }

    private void validateClubNameForUpdate(String name, Long clubId) {
        if (clubRepository.existsByNameAndIdNot(name, clubId)) {
            throw new ClubException(ClubErrorResult.CLUB_NAME_ALREADY_EXISTS);
        }
    }

    private void validateJoinRequestStatus(ClubJoinRequest request) {
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new ClubException(ClubErrorResult.INVALID_REQUEST_STATUS);
        }
    }

    private void validateCity(String city) {
        if (!"전국".equals(city) && !city.endsWith("시")) {
            throw new ClubException(ClubErrorResult.INVALID_CITY_FORMAT);
        }
    }
}
