package com.pm.catalogservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pm.catalogservice.dto.request.SearchSongRequestDto;
import com.pm.catalogservice.exception.KafkaServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import track.events.CreatedTrackEvent;
import track.events.SearchSongEvent;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaServiceTest {

    @Mock
    private CatalogService catalogService;

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Mock
    private Validator validator;

    @InjectMocks
    private KafkaService kafkaService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================================
    // ===== TEST kafkaConsumer ==========
    // ===================================

    @Test
    void kafkaConsumer_shouldParseAndCallCatalogService() throws Exception {
        CreatedTrackEvent event = CreatedTrackEvent.newBuilder()
                .setTitle("Hello")
                .setAlbum("25")
                .addArtists("Adele")
                .setDurationMs(123)
                .setStorageKey("tracks/123.mp3")
                .setCoverImageKey("covers/123.jpg")
                .build();

        byte[] bytes = event.toByteArray();

        kafkaService.kafkaConsumer(bytes);

        verify(catalogService).ingestTrack(any(CreatedTrackEvent.class));
    }

    @Test
    void kafkaConsumer_shouldHandleInvalidProtocolBufferException() throws Exception {
        byte[] invalidBytes = new byte[]{0, 1, 2};

        // mock static parseFrom để ném lỗi
        try (MockedStatic<CreatedTrackEvent> mocked = mockStatic(CreatedTrackEvent.class)) {
            mocked.when(() -> CreatedTrackEvent.parseFrom(invalidBytes))
                    .thenThrow(new InvalidProtocolBufferException("bad format"));

            kafkaService.kafkaConsumer(invalidBytes);

            verify(catalogService, never()).ingestTrack(any());
        }
    }

    // ===================================
    // ===== TEST sendSearchSongHistory ===
    // ===================================

    @Test
    void sendSearchSongHistory_shouldSendEventSuccessfully() {
        SearchSongRequestDto dto = new SearchSongRequestDto();
        dto.setEmail("test@example.com");
        dto.setSearch("hello");
        dto.setSearchedAt(LocalDateTime.now());

        // không có vi phạm validation
        when(validator.validate(any(SearchSongEvent.class)))
                .thenReturn(Collections.emptySet());

        kafkaService.sendSearchSongHistory(dto);

        verify(kafkaTemplate).send(eq("searched_song"), any(byte[].class));
    }

    @Test
    void sendSearchSongHistory_shouldThrowWhenValidationFails() {
        SearchSongRequestDto dto = new SearchSongRequestDto();
        dto.setEmail("bad@example.com");
        dto.setSearch("fail");
        dto.setSearchedAt(LocalDateTime.now());

        @SuppressWarnings("unchecked")
        ConstraintViolation<SearchSongEvent> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid event");
        when(validator.validate(any(SearchSongEvent.class)))
                .thenReturn(Set.of(violation));

        KafkaServiceException ex = assertThrows(KafkaServiceException.class,
                () -> kafkaService.sendSearchSongHistory(dto));

        assertTrue(ex.getMessage().contains("Invalid event"));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void sendSearchSongHistory_shouldThrowWhenKafkaSendFails() {
        SearchSongRequestDto dto = new SearchSongRequestDto();
        dto.setEmail("test@example.com");
        dto.setSearch("ok");
        dto.setSearchedAt(LocalDateTime.now());

        when(validator.validate(any(SearchSongEvent.class)))
                .thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Kafka down"))
                .when(kafkaTemplate).send(anyString(), any(byte[].class));

        KafkaServiceException ex = assertThrows(KafkaServiceException.class,
                () -> kafkaService.sendSearchSongHistory(dto));

        assertTrue(ex.getMessage().contains("Kafka down"));
    }
}
