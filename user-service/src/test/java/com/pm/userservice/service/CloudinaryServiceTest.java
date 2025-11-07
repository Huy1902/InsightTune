package com.pm.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadFile_shouldReturnSecureUrl() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "fake image data".getBytes()
        );

        // Fake Cloudinary upload result
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/v12345/test.png");

        // mock chain cloudinary.uploader().upload(...)
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // when
        String result = cloudinaryService.uploadFile(file);

        // then
        assertEquals("https://res.cloudinary.com/demo/image/upload/v12345/test.png", result);
    }

    @Test
    void uploadFile_shouldThrowIOException_whenUploadFails() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "fail.png", "image/png", "bad data".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        // then
        try {
            cloudinaryService.uploadFile(file);
        } catch (IOException e) {
            assertEquals("Upload failed", e.getMessage());
        }
    }
}
