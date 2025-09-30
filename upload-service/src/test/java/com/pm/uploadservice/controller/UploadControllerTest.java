package com.pm.uploadservice.controller;

import com.pm.uploadservice.dto.TrackUploadRequestDto;
import com.pm.uploadservice.dto.TrackUploadResponseDto;
import com.pm.uploadservice.exception.UploadServiceException;
import com.pm.uploadservice.service.UploadService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean UploadService uploadService;

  @Test
  void givenValidMultipart_whenUpload_then200AndReturnsDtoJson() throws Exception {
    // given
    MockMultipartFile file = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "fake-mp3".getBytes());

    TrackUploadResponseDto serviceResp = TrackUploadResponseDto.builder()
            .title("Love Story")
            .artists(List.of("Taylor Swift"))
            .storageKey("tracks/000123456789.mp3")
            .coverImageKey("covers/000123456789.jpeg")
            .kafkaStatus("SENT")
            .s3Status("Success")
            .build();

    when(uploadService.uploadTrack(any(TrackUploadRequestDto.class))).thenReturn(serviceResp);

    // when/then
    mockMvc.perform(multipart("/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("Love Story"))
            .andExpect(jsonPath("$.artists[0]").value("Taylor Swift"))
            .andExpect(jsonPath("$.storageKey").value("tracks/000123456789.mp3"))
            .andExpect(jsonPath("$.coverImageKey").value("covers/000123456789.jpeg"))
            .andExpect(jsonPath("$.kafkaStatus").value("SENT"))
            .andExpect(jsonPath("$.s3Status").value("Success"));

    // capture DTO passed to service
    ArgumentCaptor<TrackUploadRequestDto> cap = ArgumentCaptor.forClass(TrackUploadRequestDto.class);
    verify(uploadService).uploadTrack(cap.capture());
    assertThat(cap.getValue().getFile().getOriginalFilename()).isEqualTo("song.mp3");
    verifyNoMoreInteractions(uploadService);
  }

  @Test
  void givenServiceThrowsUploadServiceException_whenUpload_then200WithEmptyBody() throws Exception {
    // given
    MockMultipartFile file = new MockMultipartFile(
            "file", "broken.mp3", "audio/mpeg", new byte[]{1, 2});

    when(uploadService.uploadTrack(any(TrackUploadRequestDto.class)))
            .thenThrow(new UploadServiceException("something failed downstream"));

    // when/then (controller catches and returns OK with null body)
    mockMvc.perform(multipart("/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

    verify(uploadService).uploadTrack(any(TrackUploadRequestDto.class));
    verifyNoMoreInteractions(uploadService);
  }

  @Test
  void givenMissingFileParam_whenUpload_then400WithAdviceMessage() throws Exception {
    // no "file" part at all → MissingServletRequestPartException handled by advice
    mockMvc.perform(multipart("/upload")) // multipart request without a "file" part
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.startsWith("Invalid upload request:")));

    verifyNoInteractions(uploadService);
  }
}

