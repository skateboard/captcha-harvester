package me.brennan.harvester.model;

/**
 * @author Brennan
 * @since 8/19/21
 **/
public class CaptchaToken {
    private String token;
    private final String domain, type;

    public CaptchaToken(String domain, String type) {
        this.domain = domain;
        this.type = type;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public String getDomain() {
        return domain;
    }

    public String getToken() {
        return token;
    }
}
