package capstone.cycle.comment.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentException extends RuntimeException {
    private final CommentErrorResult commentErrorResult;
}
