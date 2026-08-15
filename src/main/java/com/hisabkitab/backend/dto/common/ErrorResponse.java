package com.hisabkitab.backend.dto.common;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;
}