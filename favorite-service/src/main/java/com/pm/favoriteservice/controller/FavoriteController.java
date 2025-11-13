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

    /**
     * API to add a song to the user's favorite list.
     *
     * @param authentication current authenticated user
     * @param createFavoriteRequestDto information about the track to add
     * @return FavoriteResponseDto containing the added track details
     */
    @PostMapping("/add")
    @Operation(summary = "Add favorite song", description = "need token")
    public ResponseEntity<FavoriteResponseDto> addFavorite(Authentication authentication
            ,@Valid @RequestBody CreateFavoriteRequestDto createFavoriteRequestDto) {
        String email = authentication.getName();

        return ResponseEntity.ok().body(favoriteService.addFavorite(email, createFavoriteRequestDto));
    }

    /**
     * API to retrieve all favorite tracks of the user.
     *
     * @param authentication current authenticated user
     * @return list of TrackResponseDto representing favorite tracks
     */
    @GetMapping
    @Operation(summary = "Get all favorite songs", description = "need token")
    public ResponseEntity<List<TrackResponseDto>> getFavorites(Authentication authentication) {
        String email = authentication.getName();

        Set<UUID> ids = favoriteService.getFavoriteIds(email);

        return ResponseEntity.ok().body(favoriteService.getFavoriteTracks(ids));
    }

    /**
     * API to remove a song from the user's favorite list.
     *
     * @param authentication current authenticated user
     * @param deleteFavoriteRequestDto information about the track to remove
     * @return message confirming successful deletion
     */
    @PostMapping("/delete")
    @Operation(summary = "Delete favorite song", description = "need token")
    public ResponseEntity<String> deleteFavorite(Authentication authentication
            ,@Valid @RequestBody DeleteFavoriteRequestDto  deleteFavoriteRequestDto) {
        String email = authentication.getName();
        favoriteService.deleteFavorite(email, deleteFavoriteRequestDto.getSongId());
        return ResponseEntity.ok().body("Successfully deleted favorite");
    }

    /**
     * API to check if a song exists in the user's favorite list.
     *
     * @param authentication current authenticated user
     * @param id track ID to check
     * @return true if the track exists in favorites, false otherwise
     */
    @GetMapping("/check/{id}")
    @Operation(summary = "Check favorite song in db", description = "need token, songId")
    public ResponseEntity<Boolean> checkFavorite(Authentication authentication, @PathVariable UUID id) {
        String email = authentication.getName();
        boolean check = favoriteService.checkExist(email, id);
        return ResponseEntity.ok().body(check);
    }
}
