package cbnu.subscribe_service.common.exception;

public class PointLimitExceededException extends RuntimeException{
    public PointLimitExceededException(String message) {
        super(message);
    }
}
