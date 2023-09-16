package com.example.detailedBoard.domain;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class ErrorResponse {
    private final String errorType;
    private final int statusCode;
    private final String message;
    private final String recommendedMessage;

}
