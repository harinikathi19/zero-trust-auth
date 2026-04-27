package com.example.zta.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expirationMinutes}")
  private long expMinutes;

  private Key key;

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String extractUsername(String token) {
    return parse(token).getBody().getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isExpired(token);
  }

  public String generateToken(String username, java.util.Collection<? extends GrantedAuthority> roles) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(username)
        .claim(
            "roles",
            roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(expMinutes * 60)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  private boolean isExpired(String token) {
    return parse(token).getBody().getExpiration().before(new Date());
  }

  private Jws<Claims> parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }
}

