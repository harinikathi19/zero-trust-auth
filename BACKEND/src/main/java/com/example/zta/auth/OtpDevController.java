package com.example.zta.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * DEV ONLY - For testing OTP flow
 * Remove this in production or secure it properly
 */
@RestController
@RequestMapping("/api/dev")
public class OtpDevController {

    private final OtpService otpService;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public OtpDevController(OtpService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/otp")
    public String getOtp(@RequestParam String challengeId) {
        // Only allow in dev mode (or remove this entirely in production)
        String otp = otpService.getOtpForChallenge(challengeId);
        if (otp == null) {
            return "Invalid or expired challenge ID";
        }
        return "OTP: " + otp + " (This is a dev endpoint - remove in production)";
    }
}


