package cz.kubaspatny.opendays.domainobject;

import java.util.List;

/**
 * Created by Kuba on 5/3/2015.
 */
public class AccessToken extends BaseToken {

    private String tokenType;
    private BaseToken refreshToken;
    private List<String> scope;
    private boolean expired;
    private long expiresIn;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public BaseToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(BaseToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
