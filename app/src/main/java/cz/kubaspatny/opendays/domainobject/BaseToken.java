package cz.kubaspatny.opendays.domainobject;

/**
 * Created by Kuba on 5/3/2015.
 */
public class BaseToken {

    private String value;
    private long expiration;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

}
