package service;
import com.pm.favoriteservice.dto.CreateFavoriteRequestDto;
import com.pm.favoriteservice.dto.FavoriteResponseDto;
import com.pm.favoriteservice.dto.TrackResponseDto;
import com.pm.favoriteservice.mapper.FavoriteMapper;
import com.pm.favoriteservice.model.Favorite;
import com.pm.favoriteservice.repository.FavoriteRepository;
import com.pm.favoriteservice.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        favoriteService = new FavoriteService(favoriteRepository, favoriteMapper, restTemplate);
        // Giả sử URL của catalog-service
        favoriteService.getClass().getDeclaredFields();
        // Set thủ công giá trị cho catalogUrl (vì @Value không hoạt động trong test)
        try {
            var field = FavoriteService.class.getDeclaredField("catalogUrl");
            field.setAccessible(true);
            field.set(favoriteService, "http://catalog-service/tracks");
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testAddFavorite_Success() {
        // Arrange
        String email = "test@example.com";
        UUID songId = UUID.randomUUID();

        CreateFavoriteRequestDto req = new CreateFavoriteRequestDto();
        req.setSongId(songId);

        Favorite favorite = Favorite.builder()
                .email(email)
                .songId(songId)
                .created_at(LocalDateTime.now())
                .build();

        FavoriteResponseDto responseDto = new FavoriteResponseDto();
        responseDto.setEmail(email);
        responseDto.setSongId(songId);

        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);
        when(favoriteMapper.toFavoriteResponseDto(any(Favorite.class))).thenReturn(responseDto);

        // Act
        FavoriteResponseDto result = favoriteService.addFavorite(email, req);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(songId, result.getSongId());
        verify(favoriteRepository).save(any(Favorite.class));
        verify(favoriteMapper).toFavoriteResponseDto(any(Favorite.class));
    }

    @Test
    void testGetFavoriteIds_ReturnsSet() {
        String email = "test@example.com";
        Set<UUID> expected = new HashSet<>(Set.of(UUID.randomUUID(), UUID.randomUUID()));

        when(favoriteRepository.findSongIdsByEmail(email)).thenReturn(expected);

        Set<UUID> result = favoriteService.getFavoriteIds(email);

        assertEquals(expected.size(), result.size());
        verify(favoriteRepository).findSongIdsByEmail(email);
    }

    @Test
    void testGetFavoriteTracks_Success() {
        Set<UUID> ids = Set.of(UUID.randomUUID());
        TrackResponseDto track1 = new TrackResponseDto();
        track1.setId(ids.iterator().next());
        track1.setTitle("Test Song");

        TrackResponseDto[] responseArray = {track1};

        ResponseEntity<TrackResponseDto[]> responseEntity =
                new ResponseEntity<>(responseArray, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(TrackResponseDto[].class)
        )).thenReturn(responseEntity);

        List<TrackResponseDto> result = favoriteService.getFavoriteTracks(ids);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Song", result.get(0).getTitle());
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(TrackResponseDto[].class));
    }

    @Test
    void testGetFavoriteTracks_ThrowsException() {
        Set<UUID> ids = Set.of(UUID.randomUUID());

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(TrackResponseDto[].class)
        )).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> favoriteService.getFavoriteTracks(ids));

        assertEquals("Service unavailable", exception.getMessage());
    }

    @Test
    void testDeleteFavorite_CallsRepository() {
        String email = "test@example.com";
        UUID songId = UUID.randomUUID();

        favoriteService.deleteFavorite(email, songId);

        verify(favoriteRepository).deleteByEmailAndSongId(email, songId);
    }

    @Test
    void testCheckExist_True() {
        String email = "test@example.com";
        UUID songId = UUID.randomUUID();

        when(favoriteRepository.existsByEmailAndSongId(email, songId)).thenReturn(true);

        boolean result = favoriteService.checkExist(email, songId);

        assertTrue(result);
        verify(favoriteRepository).existsByEmailAndSongId(email, songId);
    }

    @Test
    void testCheckExist_False() {
        String email = "test@example.com";
        UUID songId = UUID.randomUUID();

        when(favoriteRepository.existsByEmailAndSongId(email, songId)).thenReturn(false);

        boolean result = favoriteService.checkExist(email, songId);

        assertFalse(result);
    }
}