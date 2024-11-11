package capstone.cycle.file.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileException extends RuntimeException{

    private final FileErrorResult fileErrorResult;
}
