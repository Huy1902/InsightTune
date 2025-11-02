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
   * Cập nhật hoặc thêm mới trạng thái người dùng (UserState) trong hệ thống.
   * <p>
   * Nếu trạng thái của người dùng đã tồn tại (dựa trên email), phương thức sẽ cập nhật
   * thông tin bản ghi hiện có với {@code trackId} và {@code positionMs} mới.
   * Nếu không tồn tại, hệ thống sẽ tạo bản ghi mới trong cơ sở dữ liệu.
   * </p>
   *
   * @param userStateUpdateRequestDto đối tượng chứa thông tin cập nhật trạng thái người dùng,
   *                                  bao gồm email, trackId và vị trí phát hiện tại (positionMs)
   * @return {@link UserStateUpdateRespondDto} chứa thông điệp phản hồi khi cập nhật thành công
   * @throws RuntimeException nếu có lỗi xảy ra trong quá trình thao tác với cơ sở dữ liệu
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
