package capstone.cycle.common.domain.error;

import capstone.cycle.club.error.ClubErrorResult;
import capstone.cycle.club.error.ClubException;
import capstone.cycle.comment.error.CommentErrorResult;
import capstone.cycle.comment.error.CommentException;
import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.like.error.LikeErrorResult;
import capstone.cycle.like.error.LikeException;
import capstone.cycle.post.error.PostErrorResult;
import capstone.cycle.post.error.PostException;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 클라이언트에서 파라미터 잘못 전달한 경우
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // exception으로 부터 에러를 가져와서 list에 담는다.
        final List<String> errorList = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        // 해당 에러메세지 로그를 찍는다.
        log.warn("클라이언트로부터 잘못된 파라미터 전달됨 : {}", errorList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorList.toString()));
    }

    //사용자 정의 exception이 발생한 경우
    //userException
    @ExceptionHandler({UserException.class})
    public ResponseEntity<ErrorResponse> handleUserException(final UserException exception) {
        log.warn("UserException occur:" + exception);
        UserErrorResult errorResult = exception.getUserErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    @ExceptionHandler({PostException.class})
    public ResponseEntity<ErrorResponse> handleUserException(final PostException exception) {
        log.warn("PostException occur:" + exception);
        PostErrorResult errorResult = exception.getPostErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    //tokenException
    @ExceptionHandler({TokenException.class})
    public ResponseEntity<ErrorResponse> handleTokenException(final TokenException exception) {
        log.warn("TokenException occur:" + exception);
        TokenErrorResult errorResult = exception.getTokenErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    //parsingException
    @ExceptionHandler({ParsingException.class})
    public ResponseEntity<ErrorResponse> handleParsingException(final ParsingException exception) {
        log.warn("ParsingException occur:" + exception);
        ParsingErrorResult errorResult = exception.getParsingErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "접근이 거부되었습니다. 관리자만 접근 가능합니다."));
    }

    @ExceptionHandler({CommentException.class})
    public ResponseEntity<ErrorResponse> handleCommentException(final CommentException exception) {
        log.warn("CommentException occur: {}", exception.getCommentErrorResult().getMessage(), exception);
        CommentErrorResult errorResult = exception.getCommentErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    @ExceptionHandler({LikeException.class})
    public ResponseEntity<ErrorResponse> handleLikeException(final LikeException exception) {
        log.warn("LikeException occur: {}", exception.getLikeErrorResult().getMessage(), exception);
        LikeErrorResult errorResult = exception.getLikeErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    @ExceptionHandler({ClubException.class})
    public ResponseEntity<ErrorResponse> handleClubException(final ClubException exception) {
        log.warn("ClubException occur: {}", exception.getClubErrorResult().getMessage(), exception);
        ClubErrorResult errorResult = exception.getClubErrorResult();
        return ResponseEntity.status(errorResult.getStatus())
                .body(new ErrorResponse(errorResult.getStatus().value(), errorResult.getMessage()));
    }

    @RequiredArgsConstructor
    @Getter
    static class ErrorResponse {
        private final int code;
        private final String message;
    }
}
