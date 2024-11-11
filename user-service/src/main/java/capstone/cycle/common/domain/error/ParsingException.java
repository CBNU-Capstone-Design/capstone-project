package capstone.cycle.common.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ParsingException extends RuntimeException{

    private final ParsingErrorResult parsingErrorResult;
}
