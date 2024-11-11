package capstone.cycle.club.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubException extends RuntimeException {
    private final ClubErrorResult clubErrorResult;
}
