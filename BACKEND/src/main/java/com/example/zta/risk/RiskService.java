package com.example.zta.risk;

import com.example.zta.auth.JwtService;
import com.example.zta.device.DeviceService;
import com.example.zta.user.User;
import com.example.zta.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

  private final UserRepository userRepository;
  private final RiskLogRepository riskLogRepository;
  private final DeviceService deviceService;
  private final JwtService jwtService;

  @Value("${risk.high-threshold}")
  private int highThreshold;

  @Value("${risk.medium-threshold}")
  private int mediumThreshold;

  public RiskService(
      UserRepository userRepository,
      RiskLogRepository riskLogRepository,
      DeviceService deviceService,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.riskLogRepository = riskLogRepository;
    this.deviceService = deviceService;
    this.jwtService = jwtService;
  }

  public void logRequest(String username, HttpServletRequest request, String token, String deviceHash) {
    User user = userRepository.findByUsername(username).orElseThrow();
    List<String> reasons = new ArrayList<>();
    int score = 0;

    // Factor: token age (simple placeholder)
    score += 5;

    // Factor: suspicious path
    String path = request.getRequestURI();
    if (path.contains("/admin")) {
      score += 25;
      reasons.add("admin-path");
    }

    // Factor: too many requests (flag from rate limiter)
    String rl = request.getHeader("X-Rate-Limited");
    if ("true".equalsIgnoreCase(rl)) {
      score += 20;
      reasons.add("rate-limit");
    }

    // Fingerprint mismatch
    if (!deviceService.isKnownDevice(user, deviceHash)) {
      score += 20;
      reasons.add("unknown-device");
    }

    // IP change
    String ip = request.getRemoteAddr();
    if (user.getLastIp() != null && !user.getLastIp().equals(ip)) {
      score += 10;
      reasons.add("ip-change");
    }

    RiskLog log = new RiskLog();
    log.setUserId(user.getId());
    log.setPath(path);
    log.setMethod(request.getMethod());
    log.setRiskScore(score);
    log.setReasons(String.join(",", reasons));
    log.setIp(ip);
    log.setDeviceHash(deviceHash);
    log.setAtTime(Instant.now());

    riskLogRepository.save(log);

    if (score >= highThreshold) {
      // production: trigger step-up authentication (OTP)
    }
  }
}

