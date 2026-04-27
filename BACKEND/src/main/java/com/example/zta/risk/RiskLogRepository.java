package com.example.zta.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RiskLogRepository extends JpaRepository<RiskLog, Long> {
    Optional<RiskLog> findTopByUserIdOrderByAtTimeDesc(Long userId);
    List<RiskLog> findTop10ByUserIdOrderByAtTimeDesc(Long userId);
    
    @Query("SELECT COUNT(r) FROM RiskLog r WHERE r.userId = :userId AND r.riskScore >= :riskScore")
    long countByUserIdAndRiskScoreGreaterThanEqual(@Param("userId") Long userId, @Param("riskScore") int riskScore);
    
    @Query("SELECT COUNT(r) FROM RiskLog r WHERE r.userId = :userId AND r.riskScore >= :minScore AND r.riskScore < :maxScore")
    long countByUserIdAndRiskScoreBetween(@Param("userId") Long userId, @Param("minScore") int minScore, @Param("maxScore") int maxScore);
}




