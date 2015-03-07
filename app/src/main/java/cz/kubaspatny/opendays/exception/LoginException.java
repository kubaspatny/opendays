package cz.kubaspatny.opendays.exception;

/**
 * Created by Kuba on 7/3/2015.
 */
public class LoginException extends RuntimeException {

    private int code;

    public LoginException(String detailMessage, int code) {
        super(detailMessage);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
