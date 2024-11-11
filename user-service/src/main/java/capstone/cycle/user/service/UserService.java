package capstone.cycle.user.service;

import capstone.cycle.user.dto.*;
import capstone.cycle.user.entity.User;

public interface UserService {
    UserDTO socialLogin(SocialLoginDTO socialLoginDTO);





    DetailUserInfoDTO getUserDetailInfo(Long id);

    UserInfosDTO getAllUserInfos(Long id);


    void updateNickname(String nickname, Long id);

    void updateLocation(updateLocationDTO updateLocationDTO, Long id);

    UserProfileResponse getUserProfile(Long targetUserId, Long currentUserId);

    public UserProfileResponse getMyProfile(Long userId);

    void logout(String accessToken, Long userId);
}
