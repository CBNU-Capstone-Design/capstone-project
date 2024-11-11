package cbnu.subscribe_service.common.exception;

public class WrongUserIdException extends RuntimeException{
    public WrongUserIdException(String message) {
        super(message);
    }
}
