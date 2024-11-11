package capstone.cycle.user.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException{

    private final UserErrorResult userErrorResult;
}
