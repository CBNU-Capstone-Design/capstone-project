package capstone.cycle.post.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostException extends RuntimeException {
    private final PostErrorResult postErrorResult;
}
