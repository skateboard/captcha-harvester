package me.brennan.harvester;

import me.brennan.harvester.model.CaptchaToken;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Brennan
 * @since 8/19/21
 **/
public class CaptchaManager {
    private final Queue<CaptchaToken> tokenQueue = new LinkedList<>();

    public void addCaptcha(String domain, String type) {
        tokenQueue.add(new CaptchaToken(domain, type));
    }

    public CaptchaToken getRecent() {
        return tokenQueue.size() > 0 ? tokenQueue.remove() : null;
    }
}
