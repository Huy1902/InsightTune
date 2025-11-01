package com.pm.playingservice.filter;

import com.pm.playingservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

  private JwtUtil jwtUtil;
  private JwtRequestFilter filter;

  @BeforeEach
  void setUp() {
    jwtUtil = mock(JwtUtil.class);
    filter = new JwtRequestFilter(jwtUtil);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenValidBearerToken_whenDoFilter_thenSetsAuthenticationAndContinuesChain() throws Exception {
    // given
    String token = "valid.jwt.token";
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    Claims claims = mock(Claims.class);
    when(jwtUtil.parseClaims(token)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("alice@example.com");
    when(claims.get(eq("authorities"), eq(List.class))).thenReturn(List.of("ROLE_USER", "ARTIST"));

    // when
    filter.doFilter(req, res, chain);

    // then
    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    assertThat(auth.getName()).isEqualTo("alice@example.com");

    List<String> authorities = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ARTIST");

    assertThat(auth.getDetails()).isNotNull(); // details set by WebAuthenticationDetailsSource
    verify(chain, times(1)).doFilter(req, res);
    verify(jwtUtil, times(1)).parseClaims(token);
  }

  @Test
  void givenNoAuthorizationHeader_whenDoFilter_thenLeavesSecurityContextEmptyAndContinuesChain() throws Exception {
    // given
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    // when
    filter.doFilter(req, res, chain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain, times(1)).doFilter(req, res);
    verifyNoInteractions(jwtUtil);
  }

  @Test
  void givenHeaderNotStartingWithBearer_whenDoFilter_thenIgnoresAndContinuesChain() throws Exception {
    // given
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Token abc.xyz"); // not "Bearer "
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    // when
    filter.doFilter(req, res, chain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain, times(1)).doFilter(req, res);
    verifyNoInteractions(jwtUtil);
  }

  @Test
  void givenJwtParsingThrows_whenDoFilter_thenClearsSecurityContextAndContinuesChain() throws Exception {
    // given
    String token = "bad.jwt";
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    // Pre-populate context to ensure it gets cleared
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("pre-existing", null, List.of())
    );

    when(jwtUtil.parseClaims(token)).thenThrow(new JwtException("invalid"));

    // when
    filter.doFilter(req, res, chain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain, times(1)).doFilter(req, res);
    verify(jwtUtil, times(1)).parseClaims(token);
  }

  @Test
  void givenTokenWithoutAuthorities_whenDoFilter_thenSetsEmptyAuthorities() throws Exception {
    // given
    String token = "no-roles.jwt";
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    Claims claims = mock(Claims.class);
    when(jwtUtil.parseClaims(token)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("bob@example.com");
    when(claims.get(eq("authorities"), eq(List.class))).thenReturn(null); // no roles

    // when
    filter.doFilter(req, res, chain);

    // then
    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getName()).isEqualTo("bob@example.com");
    assertThat(auth.getAuthorities()).isEmpty();
    verify(chain, times(1)).doFilter(req, res);
  }
}
