package com.example.zta.device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class DeviceFingerprint {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  @Column(length = 512)
  private String fingerprintHash; // stable hash of attributes

  private String userAgent;
  private String ip;
  private String geo;
  private Instant firstSeen;
  private Instant lastSeen;
  private int seenCount;

  public DeviceFingerprint() {}

  public DeviceFingerprint(
      Long id,
      Long userId,
      String fingerprintHash,
      String userAgent,
      String ip,
      String geo,
      Instant firstSeen,
      Instant lastSeen,
      int seenCount) {
    this.id = id;
    this.userId = userId;
    this.fingerprintHash = fingerprintHash;
    this.userAgent = userAgent;
    this.ip = ip;
    this.geo = geo;
    this.firstSeen = firstSeen;
    this.lastSeen = lastSeen;
    this.seenCount = seenCount;
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

  public String getFingerprintHash() {
    return fingerprintHash;
  }

  public void setFingerprintHash(String fingerprintHash) {
    this.fingerprintHash = fingerprintHash;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getGeo() {
    return geo;
  }

  public void setGeo(String geo) {
    this.geo = geo;
  }

  public Instant getFirstSeen() {
    return firstSeen;
  }

  public void setFirstSeen(Instant firstSeen) {
    this.firstSeen = firstSeen;
  }

  public Instant getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(Instant lastSeen) {
    this.lastSeen = lastSeen;
  }

  public int getSeenCount() {
    return seenCount;
  }

  public void setSeenCount(int seenCount) {
    this.seenCount = seenCount;
  }
}

