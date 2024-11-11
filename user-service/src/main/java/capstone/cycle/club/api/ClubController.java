package capstone.cycle.club.api;

import capstone.cycle.club.dto.*;
import capstone.cycle.club.dto.request.JoinRequestCreateDTO;
import capstone.cycle.club.dto.request.JoinRequestListResponseDTO;
import capstone.cycle.club.dto.request.MyClubRequestDTO;
import capstone.cycle.club.service.ClubService;
import capstone.cycle.common.security.dto.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/c/v1/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private static final int PAGE_SIZE = 20;

    // 동호회 상세 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponseDTO> getClubDetail(
            @PathVariable Long clubId
    ) {
        ClubDetailResponseDTO club = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(club);
    }

    // 동호회 생성
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<Long> createClub(
            @RequestPart("clubCreateRequest") @Valid ClubCreateRequest request,
            @RequestPart(value = "clubImage") MultipartFile clubImage, // 클럽 생성 시 이미지 필수
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long clubId = clubService.createClub(request, clubImage, userDetails.getUser().getId());
        return ResponseEntity.ok(clubId);
    }

    // 동호회 정보 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponseDTO> updateClub(
            @PathVariable Long clubId,
            @RequestPart("clubUpdateRequest") ClubUpdateRequest request,
            @RequestPart("newClubImage") MultipartFile newClubImage,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ClubDetailResponseDTO updatedClub = clubService.updateClub(clubId, request, newClubImage, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedClub);
    }

    // 동호회 삭제
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{clubId}")
    public ResponseEntity<Void> deleteClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.deleteClub(clubId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 지역별 동호회 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<Slice<ClubListResponseDTO>> getClubsByLocation(
            @RequestParam(defaultValue = "전국") String city,
            @RequestParam(required = false) Long lastClubId
    ) {

        Slice<ClubListResponseDTO> clubs = clubService.getClubsByLocation(city, lastClubId, PAGE_SIZE);
        return ResponseEntity.ok(clubs);
    }

    // 가입한 동호회 목록 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my-clubs")
    public ResponseEntity<List<ClubSummaryDTO>> getMyClubs(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ClubSummaryDTO> clubs = clubService.getMyClubs(userDetails.getUser().getId());
        return ResponseEntity.ok(clubs);
    }

    // 동호회 가입 신청
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/requests")
    public ResponseEntity<Void> requestJoin(
            @PathVariable Long clubId,
            @RequestBody @Valid JoinRequestCreateDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.requestJoin(clubId, request, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 동호회 가입 신청 취소
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{clubId}/requests")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.cancelJoinRequest(clubId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 신청한 동호회 요청 목록
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my-requests")
    public ResponseEntity<List<MyClubRequestDTO>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<MyClubRequestDTO> requests =
                clubService.getMyRequests(userDetails.getUser().getId());
        return ResponseEntity.ok(requests);
    }

    // 동호회 가입 신청 수락
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/requests/{userId}/accept")
    public ResponseEntity<Void> acceptJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.acceptJoinRequest(clubId, userId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 동호회 가입 신청 거절
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/requests/{userId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.rejectJoinRequest(clubId, userId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 동호회원 강퇴
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/members/{userId}/expel")
    public ResponseEntity<Void> expelMember(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.expelMember(clubId, userId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 동호회 탈퇴
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{clubId}/leave")
    public ResponseEntity<Void> leaveClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.leaveClub(clubId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // 동호회 신청 목록 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{clubId}/requests")
    public ResponseEntity<List<JoinRequestListResponseDTO>> getClubJoinRequests(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<JoinRequestListResponseDTO> requests =
                clubService.getClubJoinRequests(clubId, userDetails.getUser().getId());

        return ResponseEntity.ok(requests);
    }

    // 리더 위임
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/leader/{newLeaderId}")
    public ResponseEntity<Void> transferLeadership(
            @PathVariable Long clubId,
            @PathVariable Long newLeaderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.transferLeadership(clubId, newLeaderId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // 운영진 지정
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{clubId}/managers/{userId}")
    public ResponseEntity<Void> assignManager(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.assignManager(clubId, userId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // 운영진 해제
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{clubId}/managers/{userId}")
    public ResponseEntity<Void> removeManager(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        clubService.removeManager(clubId, userId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // 동호회 검색 (동호회 이름 + 동호회 설명) 내용으로 찾음
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/search")
    public ResponseEntity<Slice<ClubListResponseDTO>> searchClubs(
            @RequestParam String keyword,
            @RequestParam(required = false) Long lastClubId
    ) {
        Slice<ClubListResponseDTO> clubs= clubService.searchClubs(keyword, lastClubId, PAGE_SIZE);
        return ResponseEntity.ok(clubs);
    }
}
