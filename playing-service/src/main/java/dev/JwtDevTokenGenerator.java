package dev;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class JwtDevTokenGenerator {
  public static void main(String[] args) {
    String secret = System.getenv().getOrDefault(
            "SUNNO_JWT_SHARED_SECRET",
            "secret_key_change_me_please_32_bytes_min");

    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    String jwt = Jwts.builder()
            .setSubject("user_000001")
            .claim("authorities", List.of("ROLE_USER"))
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    System.out.println(jwt);
  }
}
