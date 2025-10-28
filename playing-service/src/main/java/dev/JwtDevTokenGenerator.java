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
            "Q+W+8at86K9vMPRa3ZZEHWOL6wiQzDmNS0X9Wu8omL2h5qgfIIb0oEchMm+v49MP");

    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    String jwt = Jwts.builder()
            .setSubject("bob@example.com")
            .claim("authorities", List.of("ROLE_USER"))
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60 * 24 * 30)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    System.out.println(jwt);
  }
}
