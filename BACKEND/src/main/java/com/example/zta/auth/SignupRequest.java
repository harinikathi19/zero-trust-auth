package com.example.zta.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupRequest {
  @NotBlank private String username;
  @NotBlank private String password;
  private String deviceHash;
  private Map<String, Object> deviceDetails;
  private String userAgent;
  private String geo;

  public SignupRequest() {}

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDeviceHash() {
    return deviceHash;
  }

  public void setDeviceHash(String deviceHash) {
    this.deviceHash = deviceHash;
  }

  public Map<String, Object> getDeviceDetails() {
    return deviceDetails;
  }

  public void setDeviceDetails(Map<String, Object> deviceDetails) {
    this.deviceDetails = deviceDetails;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getGeo() {
    return geo;
  }

  public void setGeo(String geo) {
    this.geo = geo;
  }
}

