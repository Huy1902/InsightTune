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
     * API thêm bài hát vào danh sách yêu thích.
     *
     * @param authentication thông tin người dùng hiện tại
     * @param createFavoriteRequestDto thông tin bài hát cần thêm
     * @return FavoriteResponseDto thông tin bài hát đã thêm
     */
    @PostMapping("/add")
    @Operation(summary = "Add favorite song", description = "need token")
    public ResponseEntity<FavoriteResponseDto> addFavorite(Authentication authentication
            ,@Valid @RequestBody CreateFavoriteRequestDto createFavoriteRequestDto) {
        String email = authentication.getName();

        return ResponseEntity.ok().body(favoriteService.addFavorite(email, createFavoriteRequestDto));
    }

    /**
     * API lấy danh sách bài hát yêu thích của người dùng.
     *
     * @param authentication thông tin người dùng hiện tại
     * @return danh sách TrackResponseDto của các bài hát yêu thích
     */
    @GetMapping
    @Operation(summary = "Get all favorite songs", description = "need token")
    public ResponseEntity<List<TrackResponseDto>> getFavorites(Authentication authentication) {
        String email = authentication.getName();

        Set<UUID> ids = favoriteService.getFavoriteIds(email);

        return ResponseEntity.ok().body(favoriteService.getFavoriteTracks(ids));
    }

    /**
     * API xóa bài hát khỏi danh sách yêu thích.
     *
     * @param authentication thông tin người dùng hiện tại
     * @param deleteFavoriteRequestDto thông tin bài hát cần xóa
     * @return thông báo xóa thành công
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
     * API kiểm tra bài hát có trong danh sách yêu thích hay không.
     *
     * @param authentication thông tin người dùng hiện tại
     * @param id ID của bài hát cần kiểm tra
     * @return true nếu bài hát có trong danh sách yêu thích, false nếu không
     */
    @GetMapping("/check/{id}")
    @Operation(summary = "Check favorite song in db", description = "need token, songId")
    public ResponseEntity<Boolean> checkFavorite(Authentication authentication, @PathVariable UUID id) {
        String email = authentication.getName();
        boolean check = favoriteService.checkExist(email, id);
        return ResponseEntity.ok().body(check);
    }
}
