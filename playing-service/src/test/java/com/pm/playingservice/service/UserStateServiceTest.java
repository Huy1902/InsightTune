package com.pm.playingservice.service;

import com.pm.playingservice.dto.UserStateRespondDto;
import com.pm.playingservice.dto.UserStateUpdateRequestDto;
import com.pm.playingservice.dto.UserStateUpdateRespondDto;
import com.pm.playingservice.model.UserState;
import com.pm.playingservice.repo.UserStateRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests (Mockito) + validation tests (nested Spring context).
 * Keep the class name and original tests; add @Valid coverage below.
 */
@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

  @Mock
  private UserStateRepository userStateRepository;

  @InjectMocks
  private UserStateService userStateService;

  private UserStateUpdateRequestDto updateRequestExisting;
  private UserStateUpdateRequestDto updateRequestNew;

  @BeforeEach
  void setUp() {
    updateRequestExisting = new UserStateUpdateRequestDto(
            "alice@example.com", "track-123", 42_000
    );
    updateRequestNew = new UserStateUpdateRequestDto(
            "bob@example.com", "track-999", 7_000
    );
  }

  @Test
  void givenExistingUserState_whenUpsert_thenUpdatesFieldsAndDoesNotSave() {
    // given
    UserState existing = UserState.builder()
            .email("alice@example.com")
            .trackId("old-track")
            .positionMs(1_000)
            .build();
    when(userStateRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(existing));

    // when
    UserStateUpdateRespondDto resp = userStateService.upsert(updateRequestExisting);

    // then
    assertThat(existing.getTrackId()).isEqualTo("track-123");
    assertThat(existing.getPositionMs()).isEqualTo(42_000);
    assertThat(resp.status()).isEqualTo("Successfully updated user state");

    // save() should NOT be called for existing entity (managed within Tx)
    verify(userStateRepository, times(1)).findByEmail("alice@example.com");
    verify(userStateRepository, never()).save(any(UserState.class));
    verifyNoMoreInteractions(userStateRepository);
  }

  @Test
  void givenNonExistingUserState_whenUpsert_thenSavesEntity() {
    // given
    when(userStateRepository.findByEmail("bob@example.com"))
            .thenReturn(Optional.empty());

    // when
    UserStateUpdateRespondDto resp = userStateService.upsert(updateRequestNew);

    // then
    assertThat(resp.status()).isEqualTo("Successfully updated user state");

    // capture/save of new entity
    verify(userStateRepository, times(1)).findByEmail("bob@example.com");
    verify(userStateRepository, times(1)).save(argThat(us ->
            "bob@example.com".equals(us.getEmail()) &&
                    "track-999".equals(us.getTrackId()) &&
                    7_000 == us.getPositionMs()
    ));
    verifyNoMoreInteractions(userStateRepository);
  }

  @Test
  void givenExistingUserState_whenGetUserState_thenReturnsDto() {
    // given
    UserState existing = UserState.builder()
            .email("alice@example.com")
            .trackId("track-abc")
            .positionMs(12_345)
            .build();
    when(userStateRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(existing));

    // when
    UserStateRespondDto dto = userStateService.getUserState("alice@example.com");

    // then
    assertThat(dto.trackId()).isEqualTo("track-abc");
    assertThat(dto.positionMs()).isEqualTo(12_345);

    verify(userStateRepository, times(1)).findByEmail("alice@example.com");
    verifyNoMoreInteractions(userStateRepository);
  }

  @Test
  void givenMissingUserState_whenGetUserState_thenReturnsEmptyDefaults() {
    // given
    when(userStateRepository.findByEmail("nobody@example.com"))
            .thenReturn(Optional.empty());

    // when
    UserStateRespondDto dto = userStateService.getUserState("nobody@example.com");

    // then
    assertThat(dto.trackId()).isEqualTo("");
    assertThat(dto.positionMs()).isEqualTo(0);

    verify(userStateRepository, times(1)).findByEmail("nobody@example.com");
    verifyNoMoreInteractions(userStateRepository);
  }

  /**
   * Nested validation tests that go through a Spring proxy to trigger method-parameter validation.
   * Requires:
   *  - spring-boot-starter-validation on classpath
   *  - @Validated on UserStateService
   * No @MockBean, no @EnableMethodValidation.
   */
  @Nested
  @SpringJUnitConfig(classes = MethodValidation.MethodValidationConfig.class)
  class MethodValidation {

    @Configuration
    static class MethodValidationConfig {
      @Bean LocalValidatorFactoryBean validator() { return new LocalValidatorFactoryBean(); }
      @Bean MethodValidationPostProcessor methodValidationPostProcessor(LocalValidatorFactoryBean validator) {
        var p = new MethodValidationPostProcessor();
        p.setValidator(validator);
        return p;
      }
      @Bean UserStateRepository userStateRepository() { return Mockito.mock(UserStateRepository.class); }
      @Bean UserStateService userStateService(UserStateRepository repo) { return new UserStateService(repo); }
    }

    @Autowired private UserStateService userStateService;
    @Autowired private UserStateRepository userStateRepository;

    @Test
    void upsert_withValidDto_doesNotThrow() {
      var dto = new UserStateUpdateRequestDto("alice@example.com", "track-123", 1000);
      assertThatNoException().isThrownBy(() -> userStateService.upsert(dto));
    }

    @Test
    void upsert_withBadEmail_throws_andRepoNotTouched() {
      var dto = new UserStateUpdateRequestDto("bad-email", "track-123", 1000);
      assertThatThrownBy(() -> userStateService.upsert(dto))
              .isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("email");
      verifyNoInteractions(userStateRepository);
    }

    @Test
    void upsert_withBlankTrackId_throws() {
      var dto = new UserStateUpdateRequestDto("bob@example.com", "   ", 1000);
      assertThatThrownBy(() -> userStateService.upsert(dto))
              .isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("trackId");
    }

    @Test
    void upsert_withNullPositionMs_throws() {
      var dto = new UserStateUpdateRequestDto("bob@example.com", "track-999", null);
      assertThatThrownBy(() -> userStateService.upsert(dto))
              .isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("positionMs");
    }

    @Test
    void getUserState_withBadEmail_throws_andRepoNotTouched() {
      assertThatThrownBy(() -> userStateService.getUserState("oops"))
              .isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("email");
      verifyNoInteractions(userStateRepository);
    }

    @Test
    void getUserState_withValidEmail_doesNotThrow() {
      assertThatNoException().isThrownBy(() -> userStateService.getUserState("carol@example.com"));
    }
  }
}
