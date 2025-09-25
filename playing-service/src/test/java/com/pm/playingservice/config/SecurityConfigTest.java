package com.pm.playingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.playingservice.controller.PlayingController;
import com.pm.playingservice.dto.PlayRequestDto;
import com.pm.playingservice.dto.UserStateRequestDto;
import com.pm.playingservice.dto.UserStateRespondDto;
import com.pm.playingservice.dto.UserStateUpdateRequestDto;
import com.pm.playingservice.dto.UserStateUpdateRespondDto;
import com.pm.playingservice.filter.JwtRequestFilter;
import com.pm.playingservice.service.AwsUrlService;
import com.pm.playingservice.service.UserStateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// SecurityConfigTest.java
@WebMvcTest(controllers = PlayingController.class)
@Import({SecurityConfig.class, SecurityConfigTest.PassThroughJwtConfig.class})
class SecurityConfigTest {

  @TestConfiguration
  static class PassThroughJwtConfig {
    @Bean
    JwtRequestFilter jwtRequestFilter() {
      // Provide a JwtRequestFilter that always passes the request along
      return new JwtRequestFilter(org.mockito.Mockito.mock(com.pm.playingservice.util.JwtUtil.class)) {
        @Override
        protected void doFilterInternal(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException {
          chain.doFilter(request, response);
        }
      };
    }
  }

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  // Keep real security chain enabled; DO NOT mock JwtRequestFilter
  @org.springframework.test.context.bean.override.mockito.MockitoBean
  AwsUrlService awsUrlService;

  @org.springframework.test.context.bean.override.mockito.MockitoBean
  UserStateService userStateService;


  // ---------- /user_state (GET) ----------
  @Test
  void givenNoAuth_whenGetUserState_then401() throws Exception {
    mockMvc.perform(get("/user_state"))
            .andExpect(status().isUnauthorized());

    verifyNoInteractions(userStateService, awsUrlService);
  }

  @Test
  void givenAuthenticatedUser_whenGetUserState_then200() throws Exception {
    when(userStateService.getUserState("alice@example.com"))
            .thenReturn(new UserStateRespondDto("track-abc", 111));

    mockMvc.perform(get("/user_state").with(user("alice@example.com")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackId").value("track-abc"))
            .andExpect(jsonPath("$.positionMs").value(111));

    verify(userStateService).getUserState("alice@example.com");
    verifyNoInteractions(awsUrlService);
  }

  // ---------- /user_state (POST) ----------
  @Test
  void givenNoAuth_whenPostUserState_then401() throws Exception {
    var body = new UserStateRequestDto("track-1", 5000);

    mockMvc.perform(post("/user_state")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());

    verifyNoInteractions(userStateService, awsUrlService);
  }

  @Test
  void givenAuthenticatedUser_whenPostUserState_then200() throws Exception {
    var body = new UserStateRequestDto("track-1", 5000);
    when(userStateService.upsert(any(UserStateUpdateRequestDto.class)))
            .thenReturn(new UserStateUpdateRespondDto("Successfully updated user state"));

    mockMvc.perform(post("/user_state")
                    .with(user("bob@example.com"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackId").value("track-1"))
            .andExpect(jsonPath("$.positionMs").value(5000));

    verify(userStateService).upsert(any(UserStateUpdateRequestDto.class));
    verifyNoInteractions(awsUrlService);
  }

  // ---------- /play (POST) requires ROLE_USER or ROLE_ADMIN ----------
  @Test
  void givenNoAuth_whenPostPlay_then401() throws Exception {
    var req = new PlayRequestDto("tracks/123.mp3", "covers/123.jpg");

    mockMvc.perform(post("/play")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());

    verifyNoInteractions(awsUrlService);
  }

  @Test
  void givenWrongRole_whenPostPlay_then403() throws Exception {
    var req = new PlayRequestDto("tracks/123.mp3", "covers/123.jpg");

    mockMvc.perform(post("/play")
                    .with(user("guest@example.com").roles("GUEST")) // not USER/ADMIN
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());

    verifyNoInteractions(awsUrlService);
  }

  @Test
  void givenRoleUser_whenPostPlay_then200() throws Exception {
    var req = new PlayRequestDto("tracks/123.mp3", "covers/123.jpg");
    when(awsUrlService.getUrl("tracks/123.mp3")).thenReturn("https://cf/track123");
    when(awsUrlService.getUrl("covers/123.jpg")).thenReturn("https://cf/cover123");

    mockMvc.perform(post("/play")
                    .with(user("u@example.com").roles("USER")) // authorized
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackUrl").value("https://cf/track123"))
            .andExpect(jsonPath("$.coverImageUrl").value("https://cf/cover123"));

    verify(awsUrlService).getUrl("tracks/123.mp3");
    verify(awsUrlService).getUrl("covers/123.jpg");
  }

  @Test
  void givenRoleAdmin_whenPostPlay_then200() throws Exception {
    var req = new PlayRequestDto("tracks/777.mp3", "covers/777.jpg");
    when(awsUrlService.getUrl("tracks/777.mp3")).thenReturn("https://cf/track777");
    when(awsUrlService.getUrl("covers/777.jpg")).thenReturn("https://cf/cover777");

    mockMvc.perform(post("/play")
                    .with(user("admin@example.com").roles("ADMIN")) // authorized
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackUrl").value("https://cf/track777"))
            .andExpect(jsonPath("$.coverImageUrl").value("https://cf/cover777"));

    verify(awsUrlService).getUrl("tracks/777.mp3");
    verify(awsUrlService).getUrl("covers/777.jpg");
  }
}
