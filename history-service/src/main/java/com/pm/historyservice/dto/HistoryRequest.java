package com.pm.historyservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
public class HistoryRequest {
    @NotBlank
    private String trackId;
}
