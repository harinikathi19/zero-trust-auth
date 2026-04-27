package com.example.zta.controller;

import com.example.zta.device.DeviceFingerprint;
import com.example.zta.device.DeviceRepository;
import com.example.zta.risk.RiskLog;
import com.example.zta.risk.RiskLogRepository;
import com.example.zta.risk.RiskService;
import com.example.zta.user.User;
import com.example.zta.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class SampleController {

  private final UserRepository userRepository;
  private final RiskService riskService;
  private final RiskLogRepository riskLogRepository;
  private final DeviceRepository deviceRepository;

  public SampleController(UserRepository userRepository, RiskService riskService, 
                          RiskLogRepository riskLogRepository, DeviceRepository deviceRepository) {
    this.userRepository = userRepository;
    this.riskService = riskService;
    this.riskLogRepository = riskLogRepository;
    this.deviceRepository = deviceRepository;
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
  public Map<String, Object> userData(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    User user = userRepository.findByUsername(username).orElse(null);
    
    Map<String, Object> response = new HashMap<>();
    response.put("username", username);
    
    // Get user role
    String role = user.getRoles().stream()
        .map(r -> r.getName().replace("ROLE_", "").toLowerCase())
        .collect(Collectors.joining(", "));
    response.put("role", role.isEmpty() ? "user" : role);
    
    // Get latest risk score from RiskLog table (most recent request)
    if (user != null) {
      RiskLog latestRiskLog = riskLogRepository.findTopByUserIdOrderByAtTimeDesc(user.getId()).orElse(null);
      if (latestRiskLog != null) {
        response.put("riskScore", latestRiskLog.getRiskScore());
        response.put("riskReasons", latestRiskLog.getReasons());
        response.put("lastRiskCheck", latestRiskLog.getAtTime());
      } else {
        String deviceHash = request.getHeader("X-Device-Hash");
        int riskScore = calculateCurrentRiskScore(user, deviceHash, request);
        response.put("riskScore", riskScore);
      }
      
      // User account info
      response.put("lastLoginAt", user.getLastLoginAt());
      response.put("failedLogins", user.getFailedLogins());
      response.put("lastIp", user.getLastIp());
      response.put("lastDeviceHash", user.getLastDeviceHash());
      
      // Device fingerprint info
      String deviceHash = request.getHeader("X-Device-Hash");
      if (deviceHash != null) {
        DeviceFingerprint device = deviceRepository.findByUserIdAndFingerprintHash(user.getId(), deviceHash).orElse(null);
        if (device != null) {
          Map<String, Object> deviceInfo = new HashMap<>();
          deviceInfo.put("userAgent", device.getUserAgent());
          deviceInfo.put("ip", device.getIp());
          deviceInfo.put("geo", device.getGeo());
          deviceInfo.put("firstSeen", device.getFirstSeen());
          deviceInfo.put("lastSeen", device.getLastSeen());
          deviceInfo.put("seenCount", device.getSeenCount());
          deviceInfo.put("isKnownDevice", true);
          response.put("currentDevice", deviceInfo);
        } else {
          Map<String, Object> deviceInfo = new HashMap<>();
          deviceInfo.put("isKnownDevice", false);
          deviceInfo.put("userAgent", request.getHeader("User-Agent"));
          deviceInfo.put("ip", request.getRemoteAddr());
          response.put("currentDevice", deviceInfo);
        }
      }
      
      // Get all registered devices
      List<DeviceFingerprint> devices = deviceRepository.findByUserId(user.getId());
      List<Map<String, Object>> deviceList = devices.stream().map(d -> {
        Map<String, Object> dInfo = new HashMap<>();
        dInfo.put("userAgent", d.getUserAgent());
        dInfo.put("ip", d.getIp());
        dInfo.put("firstSeen", d.getFirstSeen());
        dInfo.put("lastSeen", d.getLastSeen());
        dInfo.put("seenCount", d.getSeenCount());
        dInfo.put("isCurrent", d.getFingerprintHash().equals(deviceHash));
        return dInfo;
      }).collect(Collectors.toList());
      response.put("registeredDevices", deviceList);
      
      // Get recent risk logs (last 10)
      List<RiskLog> recentRisks = riskLogRepository.findTop10ByUserIdOrderByAtTimeDesc(user.getId());
      List<Map<String, Object>> riskHistory = recentRisks.stream().map(r -> {
        Map<String, Object> riskInfo = new HashMap<>();
        riskInfo.put("riskScore", r.getRiskScore());
        riskInfo.put("reasons", r.getReasons());
        riskInfo.put("path", r.getPath());
        riskInfo.put("method", r.getMethod());
        riskInfo.put("atTime", r.getAtTime());
        riskInfo.put("ip", r.getIp());
        return riskInfo;
      }).collect(Collectors.toList());
      response.put("riskHistory", riskHistory);
      
      // Statistics
      Map<String, Object> stats = new HashMap<>();
      long totalRequests = riskLogRepository.count();
      long highRiskCount = riskLogRepository.countByUserIdAndRiskScoreGreaterThanEqual(user.getId(), 70);
      long mediumRiskCount = riskLogRepository.countByUserIdAndRiskScoreBetween(user.getId(), 40, 70);
      stats.put("totalRequests", totalRequests);
      stats.put("highRiskRequests", highRiskCount);
      stats.put("mediumRiskRequests", mediumRiskCount);
      stats.put("lowRiskRequests", totalRequests - highRiskCount - mediumRiskCount);
      stats.put("registeredDevicesCount", devices.size());
      response.put("statistics", stats);
    }
    
    return response;
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String adminData() {
    return "Admin data allowed";
  }

  private int calculateCurrentRiskScore(User user, String deviceHash, HttpServletRequest request) {
    int score = 0;
    
    // Device mismatch
    if (user.getLastDeviceHash() != null && deviceHash != null && !user.getLastDeviceHash().equals(deviceHash)) {
      score += 20;
    }
    
    // IP change
    String currentIp = request.getRemoteAddr();
    if (user.getLastIp() != null && !user.getLastIp().equals(currentIp)) {
      score += 10;
    }
    
    // Suspicious path
    String path = request.getRequestURI();
    if (path.contains("/admin")) {
      score += 25;
    }
    
    return Math.min(score, 100);
  }
}




