package com.pm.playingservice.service;

import com.pm.playingservice.dto.UserStateRespondDto;
import com.pm.playingservice.dto.UserStateUpdateRequestDto;
import com.pm.playingservice.dto.UserStateUpdateRespondDto;
import com.pm.playingservice.model.UserState;
import com.pm.playingservice.repo.UserStateRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserStateService {
  private final UserStateRepository userStateRepository;

  /**
   * Update or insert a user's state (UserState) in the system.
   * <p>
   * If the user's state already exists (based on email), the method will update
   * the existing record with the new {@code trackId} and {@code positionMs}.
   * If it does not exist, a new record will be created in the database.
   * </p>
   *
   * @param userStateUpdateRequestDto object containing the user's state update information,
   *                                  including email, trackId, and current playback position (positionMs)
   * @return {@link UserStateUpdateRespondDto} containing a response message after successful update
   * @throws RuntimeException if any error occurs during database operations
   *
   * @see UserState
   * @see UserStateRepository
   */
  @Transactional
  public UserStateUpdateRespondDto upsert(@Valid UserStateUpdateRequestDto userStateUpdateRequestDto) {
    Optional<UserState> userState = userStateRepository.findByEmail(userStateUpdateRequestDto.email());

    if (userState.isPresent()) {
      UserState existingUserState = userState.get();
      existingUserState.setTrackId(userStateUpdateRequestDto.trackId());
      existingUserState.setPositionMs(userStateUpdateRequestDto.positionMs());

      log.info("Update existing user state email {}, trackId {}, positionMs {}", existingUserState.getEmail(),
              existingUserState.getTrackId(), existingUserState.getPositionMs());
    }
    else {
      UserState newUserState = UserState.builder()
              .email(userStateUpdateRequestDto.email())
              .trackId(userStateUpdateRequestDto.trackId())
              .positionMs(userStateUpdateRequestDto.positionMs())
              .build();
      userStateRepository.save(newUserState);
    }
    return new UserStateUpdateRespondDto("Successfully updated user state");
  }

  public UserStateRespondDto getUserState(@Email String email) {
    Optional<UserState> userState = userStateRepository.findByEmail(email);
    if (userState.isPresent()) {
      UserState existingUserState = userState.get();
      return new UserStateRespondDto(existingUserState.getTrackId(), existingUserState.getPositionMs());
    }
    return new UserStateRespondDto("", 0);
  }
}
