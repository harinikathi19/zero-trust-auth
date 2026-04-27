package com.example.zta.device;

import com.example.zta.user.User;
import com.example.zta.user.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final UserRepository userRepository;

  public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository) {
    this.deviceRepository = deviceRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void recordAndValidate(String username, String deviceHash, String userAgent, String geoOrIp) {
    User user = userRepository.findByUsername(username).orElseThrow();
    if (deviceHash == null || deviceHash.isBlank()) {
      return;
    }

    DeviceFingerprint fp =
        deviceRepository
            .findByUserIdAndFingerprintHash(user.getId(), deviceHash)
            .orElseGet(
                () -> {
                  DeviceFingerprint df = new DeviceFingerprint();
                  df.setUserId(user.getId());
                  df.setFingerprintHash(deviceHash);
                  df.setFirstSeen(Instant.now());
                  df.setSeenCount(0);
                  return df;
                });

    fp.setUserAgent(userAgent);
    fp.setIp(geoOrIp);
    fp.setGeo(geoOrIp);
    fp.setLastSeen(Instant.now());
    fp.setSeenCount(fp.getSeenCount() + 1);
    deviceRepository.save(fp);

    user.setLastDeviceHash(deviceHash);
    userRepository.save(user);
  }

  public boolean isKnownDevice(User user, String deviceHash) {
    if (deviceHash == null || deviceHash.isBlank()) {
      return false;
    }
    return deviceRepository.existsByUserIdAndFingerprintHash(user.getId(), deviceHash);
  }
}

