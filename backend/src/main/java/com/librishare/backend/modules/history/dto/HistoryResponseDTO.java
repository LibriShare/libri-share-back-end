package com.librishare.backend.modules.history.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HistoryResponseDTO {
    private String actionType;
    private String description;
    private OffsetDateTime createdAt;
}