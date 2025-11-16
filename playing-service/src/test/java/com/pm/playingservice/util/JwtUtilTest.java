package com.pm.playingservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

  private static final String SECRET = "0123456789ABCDEF0123456789ABCDEF"; // 32 chars (>= 256 bits)

  private static JwtUtil utilWithSecret(String secret) {
    JwtUtil util = new JwtUtil();
    ReflectionTestUtils.setField(util, "secret", secret);
    return util;
  }

  private static String buildToken(String secret, String subject, List<String> authorities, Date expiration) {
    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    var builder = Jwts.builder()
            .setSubject(subject)
            .claim("authorities", authorities);
    if (expiration != null) {
      builder.setExpiration(expiration);
    }
    return builder.signWith(key).compact(); // algorithm inferred from key
  }

  @Test
  void givenValidToken_whenParseClaims_thenReturnsClaims() {
    // given
    JwtUtil jwtUtil = utilWithSecret(SECRET);
    String jwt = buildToken(SECRET, "alice@example.com", List.of("ROLE_USER", "ARTIST"),
            Date.from(Instant.now().plusSeconds(300)));

    // when
    Claims claims = jwtUtil.parseClaims(jwt);

    // then
    assertThat(claims.getSubject()).isEqualTo("alice@example.com");
    @SuppressWarnings("unchecked")
    List<String> roles = claims.get("authorities", List.class);
    assertThat(roles).containsExactlyInAnyOrder("ROLE_USER", "ARTIST");
  }

  @Test
  void givenTokenSignedWithDifferentSecret_whenParseClaims_thenThrowsJwtException() {
    // given
    JwtUtil jwtUtil = utilWithSecret(SECRET);
    String jwt = buildToken("DIFFERENT_SECRET_DIFFERENT_SECRET_123456", // also >= 32 chars
            "bob@example.com", List.of("ROLE_USER"), Date.from(Instant.now().plusSeconds(300)));

    // then
    assertThatThrownBy(() -> jwtUtil.parseClaims(jwt))
            .isInstanceOf(JwtException.class);
  }

  @Test
  void givenShortSecret_whenParseClaims_thenThrowsWeakKeyException() {
    JwtUtil jwtUtil = utilWithSecret("too-short"); // < 32 bytes

    assertThatThrownBy(() -> jwtUtil.parseClaims("header.payload.signature"))
            .isInstanceOf(WeakKeyException.class)
            .hasMessageContaining("size >= 256 bits");
  }

  @Test
  void givenExpiredToken_whenParseClaims_thenThrowsExpiredJwtException() {
    // given
    JwtUtil jwtUtil = utilWithSecret(SECRET);
    String jwt = buildToken(SECRET, "eve@example.com", List.of("ROLE_USER"),
            Date.from(Instant.now().minusSeconds(60))); // already expired

    // then
    assertThatThrownBy(() -> jwtUtil.parseClaims(jwt))
            .isInstanceOf(ExpiredJwtException.class);
  }
}
