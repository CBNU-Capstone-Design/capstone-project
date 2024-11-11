package capstone.cycle.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialProvider {

    KAKAO("KAKAO"),
    NAVER("NAVER"),
    GOOGLE("GOOGLE");

    private final String provider;
}
