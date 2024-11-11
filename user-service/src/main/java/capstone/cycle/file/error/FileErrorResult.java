package capstone.cycle.file.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorResult {

    EMPTY_FILE(HttpStatus.BAD_REQUEST, "요청 파일이 정상적으로 전송되지 않았습니다."),
    UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패하였습니다."),
    FILE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청 파일 그룹을 찾을 수 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청 파일을 찾을 수 없습니다."),
    DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "요청 파일의 삭제를 실패했습니다.")
    ;

    private final HttpStatus status;
    private final String message;

}
