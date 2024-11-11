package cbnu.subscribe_service.common.exception;

public class SubscriptionExpiredException extends RuntimeException{
    public SubscriptionExpiredException(String message) {
        super(message);
    }
}
