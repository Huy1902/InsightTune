package com.pm.playingservice.controller;

import com.pm.playingservice.dto.*;
import com.pm.playingservice.service.AwsUrlService;
import com.pm.playingservice.service.KafkaService;
import com.pm.playingservice.service.UserStateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/play")
public class PlayingController {
  private final AwsUrlService awsUrlService;
  private final UserStateService userStateService;
  private final KafkaService kafkaService;

  /**
   * Nhận yêu cầu phát nhạc và trả về link nghe nhạc cùng link ảnh bìa.
   *
   * @param req thông tin bài hát cần phát
   * @param auth thông tin xác thực người dùng
   * @return link nhạc và ảnh bìa
   * @throws Exception nếu xảy ra lỗi khi lấy URL
   */
  @PostMapping
  @Operation(summary = "Receive a play request then send back a play response contain a mp3 link and image link")
  public ResponseEntity<PlayResponseDto> play(@RequestBody @Valid PlayRequestDto req,
                                              Authentication auth) throws Exception {
    String trackUrl = awsUrlService.getUrl(req.storageKey());
    String imageUrl = "";
    if (!req.coverImageKey().isBlank()) {
      imageUrl = awsUrlService.getUrl(req.coverImageKey());
    }

    kafkaService.sendPlayTrack(new PlayTrackDto(auth.getName(), req.storageKey(), LocalDateTime.now()));

    return ResponseEntity.ok().body(new PlayResponseDto(trackUrl, imageUrl));
  }

  /**
   * Lấy link ảnh hoặc file từ AWS S3 dựa trên key.
   *
   * @param key khóa lưu trữ file trên S3
   * @return link truy cập file tạm thời
   * @throws Exception nếu không thể tạo URL
   */
  @GetMapping("/url")
  @Operation(summary = "Get an image link of a song")
  public ResponseEntity<LinkRespondDto> getLink(@RequestParam String key) throws Exception {
    String url = awsUrlService.getUrl(key);
    return ResponseEntity.ok().body(new LinkRespondDto(url));
  }

  /**
   * Cập nhật trạng thái phát nhạc của người dùng.
   *
   * @param userStateRequestDto thông tin trạng thái phát nhạc
   * @param auth thông tin xác thực người dùng
   * @return trạng thái phát nhạc đã cập nhật
   * @throws Exception nếu không thể cập nhật
   */
  @PostMapping("/user_state")
  @Operation(summary = "Update user state of an user")
  public ResponseEntity<UserStateRespondDto> updateUserState(@RequestBody UserStateRequestDto userStateRequestDto,
                                                             Authentication auth) throws Exception {
    String email = auth.getName();
    UserStateUpdateRespondDto userStateUpdateRespondDto = userStateService.upsert(new UserStateUpdateRequestDto(email,
            userStateRequestDto.trackId(), userStateRequestDto.positionMs()));
    log.info("Update user state for email: {} with status {}", email, userStateUpdateRespondDto.status());
    return ResponseEntity.ok().body(new UserStateRespondDto(userStateRequestDto.trackId(), userStateRequestDto.positionMs()));

  }

  /**
   * Lấy trạng thái phát nhạc gần nhất của người dùng.
   *
   * @param auth thông tin xác thực người dùng
   * @return trạng thái phát nhạc hiện tại của người dùng
   * @throws Exception nếu không tìm thấy hoặc xảy ra lỗi khi truy vấn
   */
  @GetMapping("/user_state")
  @Operation(summary = "Find user state by email")
  public ResponseEntity<UserStateRespondDto> findUserState(Authentication auth) throws Exception {
    String email = auth.getName();
    UserStateRespondDto userStateRespondDto = userStateService.getUserState(email);
    return ResponseEntity.ok().body(userStateRespondDto);
  }

}
