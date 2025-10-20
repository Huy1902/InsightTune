package com.pm.favoriteservice.controller;



import com.pm.favoriteservice.dto.CreateFavoriteRequestDto;
import com.pm.favoriteservice.dto.DeleteFavoriteRequestDto;
import com.pm.favoriteservice.dto.FavoriteResponseDto;
import com.pm.favoriteservice.dto.TrackResponseDto;
import com.pm.favoriteservice.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/add")
    @Operation(summary = "Add favorite song", description = "need token")
    public ResponseEntity<FavoriteResponseDto> addFavorite(Authentication authentication
            ,@Valid @RequestBody CreateFavoriteRequestDto createFavoriteRequestDto) {
        String email = authentication.getName();

        return ResponseEntity.ok().body(favoriteService.addFavorite(email, createFavoriteRequestDto));
    }

    @GetMapping
    @Operation(summary = "Get all favorite songs", description = "need token")
    public ResponseEntity<List<TrackResponseDto>> getFavorites(Authentication authentication) {
        String email = authentication.getName();

        Set<UUID> ids = favoriteService.getFavoriteIds(email);

        return ResponseEntity.ok().body(favoriteService.getFavoriteTracks(ids));
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete favorite song", description = "need token")
    public ResponseEntity<String> deleteFavorite(Authentication authentication
            ,@Valid @RequestBody DeleteFavoriteRequestDto  deleteFavoriteRequestDto) {
        String email = authentication.getName();
        favoriteService.deleteFavorite(email, deleteFavoriteRequestDto.getSongId());
        return ResponseEntity.ok().body("Successfully deleted favorite");
    }

    @GetMapping("/check/{id}")
    @Operation(summary = "Check favorite song in db", description = "need token, songId")
    public ResponseEntity<Boolean> checkFavorite(Authentication authentication, @PathVariable UUID id) {
        String email = authentication.getName();
        boolean check = favoriteService.checkExist(email, id);
        return ResponseEntity.ok().body(check);
    }
}
