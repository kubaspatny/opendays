package cz.kubaspatny.opendays.exception;

/**
 * Created by Kuba on 11/3/2015.
 */
public class ErrorCodeException extends RuntimeException {

    private int mErrorCode;

    public ErrorCodeException(String detailMessage, int mErrorCode) {
        super(detailMessage);
        this.mErrorCode = mErrorCode;
    }

    public int getmErrorCode() {
        return mErrorCode;
    }

}
