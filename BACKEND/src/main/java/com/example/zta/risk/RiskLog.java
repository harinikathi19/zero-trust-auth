package com.example.zta.risk;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class RiskLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private String path;
  private String method;
  private int riskScore;
  private String reasons;
  private String ip;
  private String deviceHash;
  private Instant atTime;

  public RiskLog() {}

  public RiskLog(
      Long id,
      Long userId,
      String path,
      String method,
      int riskScore,
      String reasons,
      String ip,
      String deviceHash,
      Instant atTime) {
    this.id = id;
    this.userId = userId;
    this.path = path;
    this.method = method;
    this.riskScore = riskScore;
    this.reasons = reasons;
    this.ip = ip;
    this.deviceHash = deviceHash;
    this.atTime = atTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public int getRiskScore() {
    return riskScore;
  }

  public void setRiskScore(int riskScore) {
    this.riskScore = riskScore;
  }

  public String getReasons() {
    return reasons;
  }

  public void setReasons(String reasons) {
    this.reasons = reasons;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getDeviceHash() {
    return deviceHash;
  }

  public void setDeviceHash(String deviceHash) {
    this.deviceHash = deviceHash;
  }

  public Instant getAtTime() {
    return atTime;
  }

  public void setAtTime(Instant atTime) {
    this.atTime = atTime;
  }
}

