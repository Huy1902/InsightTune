package com.pm.playingservice.service;

import com.pm.playingservice.dto.UserStateRespondDto;
import com.pm.playingservice.dto.UserStateUpdateRequestDto;
import com.pm.playingservice.dto.UserStateUpdateRespondDto;
import com.pm.playingservice.model.UserState;
import com.pm.playingservice.repo.UserStateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStateService {
  private final UserStateRepository userStateRepository;

  @Transactional
  public UserStateUpdateRespondDto upsert(UserStateUpdateRequestDto userStateUpdateRequestDto) {
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

  public UserStateRespondDto getUserState(String email) {
    Optional<UserState> userState = userStateRepository.findByEmail(email);
    if (userState.isPresent()) {
      UserState existingUserState = userState.get();
      return new UserStateRespondDto(existingUserState.getTrackId(), existingUserState.getPositionMs());
    }
    return new UserStateRespondDto("", 0);
  }
}
