package com.example.zta.config;

import com.example.zta.auth.JwtService;
import com.example.zta.device.DeviceService;
import com.example.zta.risk.RiskService;
import com.example.zta.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userService;
  private final DeviceService deviceService;
  private final RiskService riskService;

  public JwtFilter(
      JwtService jwtService, UserService userService,
      DeviceService deviceService, RiskService riskService) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.deviceService = deviceService;
    this.riskService = riskService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, jakarta.servlet.ServletException {

    String authHeader = request.getHeader("Authorization");
    String bearerPrefix = "Bearer ";
    String username = null;
    String token = null;

    if (authHeader != null && authHeader.startsWith(bearerPrefix)) {
      token = authHeader.substring(bearerPrefix.length());
      username = jwtService.extractUsername(token);
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userService.loadUserByUsername(username);

      if (jwtService.isTokenValid(token, userDetails)) {

        String deviceHash = request.getHeader("X-Device-Hash");
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        deviceService.recordAndValidate(username, deviceHash, userAgent, ip);

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        riskService.logRequest(username, request, token, deviceHash);
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    // ⛔ DO NOT FILTER SIGNUP & LOGIN & OPTIONS
    return request.getMethod().equalsIgnoreCase("OPTIONS")
        || path.startsWith("/api/auth/");
  }
}
