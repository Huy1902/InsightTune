package com.pm.favoriteservice.service;

import com.pm.favoriteservice.dto.CreateFavoriteRequestDto;
import com.pm.favoriteservice.dto.FavoriteResponseDto;
import com.pm.favoriteservice.dto.TrackResponseDto;
import com.pm.favoriteservice.mapper.FavoriteMapper;
import com.pm.favoriteservice.model.Favorite;
import com.pm.favoriteservice.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FavoriteService {

    @Value("${catalog-service.url}")
    private String catalogUrl;

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final RestTemplate restTemplate;

    public FavoriteService(FavoriteRepository favoriteRepository, FavoriteMapper favoriteMapper, RestTemplate restTemplate) {
        this.favoriteRepository = favoriteRepository;
        this.favoriteMapper = favoriteMapper;
        this.restTemplate = restTemplate;
    }

    public FavoriteResponseDto addFavorite(String email, CreateFavoriteRequestDto createFavoriteRequestDto) {
        Favorite favorite = Favorite.builder()
                .email(email)
                .songId(createFavoriteRequestDto.getSongId())
                .created_at(LocalDateTime.now())
                .build();

        return favoriteMapper.toFavoriteResponseDto(favoriteRepository.save(favorite));
    }

    public Set<UUID> getFavoriteIds(String email) {
        return favoriteRepository.findSongIdsByEmail(email);
    }

    public List<TrackResponseDto> getFavoriteTracks(Set<UUID> ids) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Set<UUID>> request = new HttpEntity<>(ids, headers);

            ResponseEntity<TrackResponseDto[]> response = restTemplate.postForEntity(
                    catalogUrl,
                    request,
                    TrackResponseDto[].class
            );

            return Arrays.asList(Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void deleteFavorite(String email, UUID id) {
        favoriteRepository.deleteByEmailAndSongId(email, id);
    }

    public boolean checkExist(String email, UUID id) {
        return favoriteRepository.existsByEmailAndSongId(email, id);
    }
}
