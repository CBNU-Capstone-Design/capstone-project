package capstone.cycle.user.api;

import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.user.dto.*;
import capstone.cycle.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/u/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my/info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401",
                    description = "1. 엑세스 토큰이 없을 때 \t\n 2. 엑세스 토큰이 만료되었을 때 \t\n 3. 엑세스 토큰으로 유저를 찾을 수 없을 때",
                    content = @Content(schema = @Schema(example = "{\"code\" : \"401 UNAUTHORIZED\", \"message\" : \"message\"}"))),
            @ApiResponse(responseCode = "200",
                    description = "유저 정보 가져오기 성공",
                    content = @Content(schema = @Schema(implementation = DetailUserInfoDTO.class))),
    })
    public ResponseEntity<DetailUserInfoDTO> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DetailUserInfoDTO detailUserInfoDTO = userService.getUserDetailInfo(userDetails.getUser().getId());
        return ResponseEntity.ok(detailUserInfoDTO);
    }

    // 닉네임 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/my/nickname")
    public ResponseEntity<Void> updateNickname(
            @RequestBody @Valid UpdateNicknameDTO updateNicknameDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateNickname(updateNicknameDTO.getNickname(), userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 현재 위치 업데이트
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/my/location")
    public ResponseEntity<Void> updateLocation(
            @RequestBody @Valid updateLocationDTO updateLocationDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateLocation(updateLocationDTO, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfosDTO> getAllUserInfos(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserInfosDTO userInfosDTO = userService.getAllUserInfos(userDetails.getUser().getId());
        return ResponseEntity.ok(userInfosDTO);
    }


    // 특정 사용자 프로필 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UserProfileResponse profile = userService.getUserProfile(userId, userDetails.getUser().getId());

        return ResponseEntity.ok(profile);
    }

    // 내 프로필 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UserProfileResponse myProfile = userService.getMyProfile(userDetails.getUser().getId());
        return ResponseEntity.ok(myProfile);
    }
}
