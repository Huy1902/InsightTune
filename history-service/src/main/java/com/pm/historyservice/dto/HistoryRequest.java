package com.pm.historyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
public class HistoryRequest {
    @NotNull
    private UUID trackId;

    @NotBlank
    private String storageKey;
}
