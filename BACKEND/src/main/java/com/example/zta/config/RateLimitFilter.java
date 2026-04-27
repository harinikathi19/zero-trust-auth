package com.example.zta.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter implements Filter {

  private final Map<String, AtomicInteger> counter = new ConcurrentHashMap<>();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;

    String path = req.getServletPath();

    // ⛔ Skip rate limit for login/signup
    if (path.startsWith("/api/auth/")) {
      chain.doFilter(request, response);
      return;
    }

    String key = req.getRemoteAddr();
    counter.putIfAbsent(key, new AtomicInteger(0));
    int c = counter.get(key).incrementAndGet();

    if (c > 100) {
      ((HttpServletResponse) response).setHeader("X-Rate-Limited", "true");
    }

    chain.doFilter(request, response);
  }
}



