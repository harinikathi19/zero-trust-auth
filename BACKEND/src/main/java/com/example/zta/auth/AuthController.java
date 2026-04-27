package com.example.zta.auth;

import com.example.zta.device.DeviceService;
import com.example.zta.risk.AnomalyService;
import com.example.zta.user.User;
import com.example.zta.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final DeviceService deviceService;
    private final AnomalyService anomalyService;
    private final JwtService jwtService;
    private final OtpService otpService;

    @org.springframework.beans.factory.annotation.Value("${risk.high-threshold:70}")
    private int highRiskThreshold;

    public AuthController(AuthenticationManager authManager,
                          UserService userService,
                          DeviceService deviceService,
                          AnomalyService anomalyService,
                          JwtService jwtService,
                          OtpService otpService) {
        this.authManager = authManager;
        this.userService = userService;
        this.deviceService = deviceService;
        this.anomalyService = anomalyService;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (userService.exists(request.getUsername())) {
            return ResponseEntity.status(409).body("User already exists");
        }

        User user = userService.createUser(request.getUsername(), request.getPassword(), Set.of("ROLE_USER"));
        
        // Record device fingerprint on signup
        if (request.getDeviceHash() != null) {
            deviceService.recordAndValidate(user.getUsername(), request.getDeviceHash(), 
                request.getUserAgent(), request.getGeo());
            userService.updateLoginSuccess(user, request.getDeviceHash(), request.getGeo());
        }
        
        // Enable user immediately (or require email verification in production)
        user.setEnabled(true);
        userService.saveUser(user);

        return ResponseEntity.ok("Signup successful. Please login.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        User user = userService.findByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body("Account not enabled. Please validate your account.");
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Device fingerprint + anomaly score
            deviceService.recordAndValidate(user.getUsername(), request.getDeviceHash(), 
                request.getUserAgent(), request.getGeo());
            int anomalyScore = anomalyService.scoreLogin(user, request);

            // High-risk login requires OTP challenge
            if (anomalyScore >= highRiskThreshold) {
                String challengeId = otpService.generateOtp(user.getUsername(), anomalyScore);
                return ResponseEntity.ok(new LoginResponse(null, anomalyScore, true, challengeId, 
                    "High-risk login detected. OTP verification required."));
            }

            // Low-risk login - proceed normally
            String token = jwtService.generateToken(
                    user.getUsername(),
                    userService.loadUserByUsername(user.getUsername()).getAuthorities()
            );

            userService.updateLoginSuccess(user, request.getDeviceHash(), request.getGeo());

            return ResponseEntity.ok(new LoginResponse(token, anomalyScore, false, null, null));
        } catch (AuthenticationException ex) {
            userService.incrementFailedLogin(request.getUsername());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        OtpService.OtpChallenge challenge = otpService.getChallenge(request.getChallengeId());
        
        if (challenge == null) {
            return ResponseEntity.status(400).body("Invalid or expired challenge ID");
        }

        if (!otpService.verifyOtp(request.getChallengeId(), request.getOtp())) {
            return ResponseEntity.status(401).body("Invalid OTP");
        }

        // OTP verified - generate token
        User user = userService.findByUsername(challenge.getUsername());
        if (user == null || !user.isEnabled()) {
            return ResponseEntity.status(403).body("Account not enabled");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                userService.loadUserByUsername(user.getUsername()).getAuthorities()
        );

        return ResponseEntity.ok(new LoginResponse(token, challenge.getRiskScore(), false, null, "OTP verified successfully"));
    }

    public record LoginResponse(String token, int anomalyScore, boolean requiresOTP, 
                               String challengeId, String message) {}
    
    public static class OtpVerificationRequest {
        private String challengeId;
        private String otp;

        public String getChallengeId() { return challengeId; }
        public void setChallengeId(String challengeId) { this.challengeId = challengeId; }

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
}
