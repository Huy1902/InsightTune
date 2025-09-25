package com.pm.playingservice.service;

import com.pm.playingservice.dto.UserStateRespondDto;
import com.pm.playingservice.dto.UserStateUpdateRequestDto;
import com.pm.playingservice.dto.UserStateUpdateRespondDto;
import com.pm.playingservice.model.UserState;
import com.pm.playingservice.repo.UserStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
}
