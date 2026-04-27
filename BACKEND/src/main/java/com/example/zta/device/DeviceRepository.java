package com.example.zta.device;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<DeviceFingerprint, Long> {
  Optional<DeviceFingerprint> findByUserIdAndFingerprintHash(Long userId, String hash);
  boolean existsByUserIdAndFingerprintHash(Long userId, String hash);
  List<DeviceFingerprint> findByUserId(Long userId);
}




