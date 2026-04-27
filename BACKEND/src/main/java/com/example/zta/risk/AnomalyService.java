package com.example.zta.risk;

import com.example.zta.auth.AuthRequest;
import com.example.zta.user.User;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AnomalyService {

    public int scoreLogin(User user, AuthRequest req) {
        int score = 0;

        // Failed login attempts
        int failedLogins = user.getFailedLogins();
        if (failedLogins > 2) {
            score += 15;
        }

        // Time-based anomaly (midnight login = suspicious)
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        int hour = now.getHour();
        if (hour >= 0 && hour < 4) { // Midnight to 4 AM
            score += 20;
        } else if (hour < 5 || hour > 23) {
            score += 10;
        }

        // Device hash mismatch
        String lastDeviceHash = user.getLastDeviceHash();
        if (lastDeviceHash != null && req.getDeviceHash() != null && !lastDeviceHash.equals(req.getDeviceHash())) {
            score += 20;
        }

        // IP/geo mismatch
        String lastIp = user.getLastIp();
        if (lastIp != null && req.getGeo() != null && !lastIp.equals(req.getGeo())) {
            score += 15;
        }

        // Login behavior analysis
        if (req.getLoginBehavior() != null) {
            Map<String, Object> behavior = req.getLoginBehavior();
            
            // Time since last login
            if (behavior.containsKey("timeSinceLastLogin")) {
                Object timeSince = behavior.get("timeSinceLastLogin");
                if (timeSince instanceof Number) {
                    long ms = ((Number) timeSince).longValue();
                    if (ms > 0 && ms < 60000) { // Less than 1 minute = suspicious
                        score += 10;
                    }
                }
            }
            
            // Time since last failure
            if (behavior.containsKey("timeSinceLastFailure")) {
                Object timeSince = behavior.get("timeSinceLastFailure");
                if (timeSince instanceof Number) {
                    long ms = ((Number) timeSince).longValue();
                    if (ms > 0 && ms < 30000) { // Very recent failure = suspicious
                        score += 15;
                    }
                }
            }
            
            // Login attempts count
            if (behavior.containsKey("loginAttempts")) {
                Object attempts = behavior.get("loginAttempts");
                if (attempts instanceof Number) {
                    int count = ((Number) attempts).intValue();
                    if (count > 3) {
                        score += 10;
                    }
                }
            }
        }

        // Typing metrics analysis
        if (req.getTypingMetrics() != null) {
            Map<String, Object> typing = req.getTypingMetrics();
            
            // Unusually fast typing speed (bot-like)
            if (typing.containsKey("usernameTypingSpeed")) {
                Object speed = typing.get("usernameTypingSpeed");
                if (speed instanceof Number) {
                    double wpm = ((Number) speed).doubleValue();
                    if (wpm > 200) { // Unrealistically fast
                        score += 15;
                    }
                }
            }
            
            if (typing.containsKey("passwordTypingSpeed")) {
                Object speed = typing.get("passwordTypingSpeed");
                if (speed instanceof Number) {
                    double wpm = ((Number) speed).doubleValue();
                    if (wpm > 200) { // Unrealistically fast
                        score += 15;
                    }
                }
            }
            
            // Unusually slow typing (might indicate copy-paste or hesitation)
            if (typing.containsKey("usernameTypingTime")) {
                Object time = typing.get("usernameTypingTime");
                if (time instanceof Number) {
                    long ms = ((Number) time).longValue();
                    if (ms > 10000) { // More than 10 seconds for username
                        score += 5;
                    }
                }
            }
        }

        return Math.min(score, 100);
    }
}

