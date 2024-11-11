package cbnu.subscribe_service.common.exception;

public class PointBelowThresholdException extends RuntimeException{
    public PointBelowThresholdException(String message) {
        super(message);
    }
}
