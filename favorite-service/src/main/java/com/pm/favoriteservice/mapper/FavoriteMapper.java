package com.pm.favoriteservice.mapper;

import com.pm.favoriteservice.dto.FavoriteResponseDto;
import com.pm.favoriteservice.model.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {
    public FavoriteResponseDto toFavoriteResponseDto(Favorite favorite) {
        return FavoriteResponseDto.builder()
                .email(favorite.getEmail())
                .id(favorite.getId())
                .created_at(favorite.getCreated_at())
                .songId(favorite.getSongId())
                .build();
    }
}
