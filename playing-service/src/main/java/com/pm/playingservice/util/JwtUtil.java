package com.pm.playingservice.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

  @Value("${spotube.jwt.shared_secret}")
  private String secret; // use a 32+ byte secret for HS256

  private Key key() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public Claims parseClaims(String jwt) {
    return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(jwt)
            .getBody(); // throws if invalid/expired
  }
}

