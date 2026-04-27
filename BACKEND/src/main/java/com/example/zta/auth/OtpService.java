package com.example.zta.auth;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {
    
    private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    public String generateOtp(String username, int riskScore) {
        String otp = String.format("%0" + OTP_LENGTH + "d", random.nextInt((int) Math.pow(10, OTP_LENGTH)));
        String challengeId = username + "_" + System.currentTimeMillis();
        
        OtpChallenge challenge = new OtpChallenge();
        challenge.setOtp(otp);
        challenge.setUsername(username);
        challenge.setRiskScore(riskScore);
        challenge.setCreatedAt(System.currentTimeMillis());
        
        challenges.put(challengeId, challenge);
        
        // Clean up expired challenges (simple cleanup)
        cleanupExpiredChallenges();
        
        return challengeId;
    }

    public boolean verifyOtp(String challengeId, String otp) {
        OtpChallenge challenge = challenges.get(challengeId);
        if (challenge == null) {
            return false;
        }
        
        // Check expiry
        if (System.currentTimeMillis() - challenge.getCreatedAt() > OTP_EXPIRY_MS) {
            challenges.remove(challengeId);
            return false;
        }
        
        boolean isValid = challenge.getOtp().equals(otp);
        if (isValid) {
            challenges.remove(challengeId);
        }
        return isValid;
    }

    public OtpChallenge getChallenge(String challengeId) {
        OtpChallenge challenge = challenges.get(challengeId);
        if (challenge != null) {
            // Return a copy without exposing the OTP directly (for security)
            OtpChallenge copy = new OtpChallenge();
            copy.setUsername(challenge.getUsername());
            copy.setRiskScore(challenge.getRiskScore());
            copy.setCreatedAt(challenge.getCreatedAt());
            return copy;
        }
        return null;
    }
    
    // For dev/testing only - get OTP value
    public String getOtpForChallenge(String challengeId) {
        OtpChallenge challenge = challenges.get(challengeId);
        return challenge != null ? challenge.getOtp() : null;
    }

    private void cleanupExpiredChallenges() {
        long now = System.currentTimeMillis();
        challenges.entrySet().removeIf(entry -> 
            now - entry.getValue().getCreatedAt() > OTP_EXPIRY_MS
        );
    }

    public static class OtpChallenge {
        private String otp;
        private String username;
        private int riskScore;
        private long createdAt;

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    }
}

