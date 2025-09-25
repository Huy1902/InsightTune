package com.pm.playingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.playingservice.dto.*;
import com.pm.playingservice.service.AwsUrlService;
import com.pm.playingservice.service.UserStateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PlayingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.pm.playingservice.filter.JwtRequestFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false) // disables default security filters
class PlayingControllerTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @org.springframework.test.context.bean.override.mockito.MockitoBean
  AwsUrlService awsUrlService;
  @org.springframework.test.context.bean.override.mockito.MockitoBean
  UserStateService userStateService;

  @Test
  void givenValidRequest_whenPlay_thenReturnsSignedUrls() throws Exception {
    var req = new PlayRequestDto("tracks/123.mp3", "covers/123.jpg");

    when(awsUrlService.getUrl("tracks/123.mp3")).thenReturn("https://cf/track123?sig=abc");
    when(awsUrlService.getUrl("covers/123.jpg")).thenReturn("https://cf/img123?sig=xyz");

    mockMvc.perform(post("/play")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            //.andDo(print()) // uncomment to see response in logs
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.trackUrl").value("https://cf/track123?sig=abc"))
            .andExpect(jsonPath("$.coverImageUrl").value("https://cf/img123?sig=xyz"));

    verify(awsUrlService).getUrl("tracks/123.mp3");
    verify(awsUrlService).getUrl("covers/123.jpg");
    verifyNoMoreInteractions(awsUrlService);
    verifyNoInteractions(userStateService);
  }

  @Test
  void givenAuthenticatedUser_whenUpdateUserState_thenUpsertsAndEchoes() throws Exception {
    String email = "alice@example.com";
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(email);

    UserStateRequestDto body = new UserStateRequestDto("track-777", 12345);
    when(userStateService.upsert(any(UserStateUpdateRequestDto.class)))
            .thenReturn(new UserStateUpdateRespondDto("Successfully updated user state"));

    mockMvc.perform(post("/user_state")
                    .principal(auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackId").value("track-777"))
            .andExpect(jsonPath("$.positionMs").value(12345));

    var captor = org.mockito.ArgumentCaptor.forClass(UserStateUpdateRequestDto.class);
    verify(userStateService).upsert(captor.capture());
    var dto = captor.getValue();
    assertThat(dto.email()).isEqualTo(email);
    assertThat(dto.trackId()).isEqualTo("track-777");
    assertThat(dto.positionMs()).isEqualTo(12345);

    verifyNoMoreInteractions(userStateService);
    verifyNoInteractions(awsUrlService);
  }

  @Test
  void givenAuthenticatedUser_whenFindUserState_thenReturnsPersistedState() throws Exception {
    String email = "bob@example.com";
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(email);

    when(userStateService.getUserState(email))
            .thenReturn(new UserStateRespondDto("track-abc", 4242));

    mockMvc.perform(get("/user_state").principal(auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackId").value("track-abc"))
            .andExpect(jsonPath("$.positionMs").value(4242));

    verify(userStateService).getUserState(email);
    verifyNoMoreInteractions(userStateService);
    verifyNoInteractions(awsUrlService);
  }

  @Test
  void givenServiceThrows_whenPlay_thenPropagatesException() throws Exception {
    PlayRequestDto req = new PlayRequestDto("tracks/bad.mp3", "covers/bad.jpg");
    when(awsUrlService.getUrl(anyString())).thenThrow(new RuntimeException("boom"));

    // perform() throws here because no handler resolves the exception
    assertThatThrownBy(() ->
            mockMvc.perform(post("/play")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andReturn()
    )
            .isInstanceOf(jakarta.servlet.ServletException.class)
            .hasRootCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("boom");

    verify(awsUrlService).getUrl("tracks/bad.mp3");
    verifyNoMoreInteractions(awsUrlService);
    verifyNoInteractions(userStateService);
  }

  @Test
  void givenValidKey_whenGetLink_thenReturnsSignedUrl() throws Exception {
    // given
    String key = "covers/123.jpg";
    String signed = "https://cf.example/covers/123.jpg?sig=xyz";
    when(awsUrlService.getUrl(key)).thenReturn(signed);

    // when/then
    mockMvc.perform(get("/url?key=" + key))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.url").value(signed));

    verify(awsUrlService, times(1)).getUrl(key);
    verifyNoMoreInteractions(awsUrlService);
    verifyNoInteractions(userStateService);
  }

  @Test
  void givenMissingKeyParam_whenGetLink_then400() throws Exception {
    mockMvc.perform(get("/url"))
            .andExpect(status().isBadRequest()); // Spring MVC returns 400 for missing required @RequestParam

    verifyNoInteractions(awsUrlService, userStateService);
  }
}
