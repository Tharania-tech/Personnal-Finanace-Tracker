package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ApiResponse - Generic Response Wrapper
 *
 * Wraps all API responses in a consistent envelope format.
 *
 * Example Success Response:
 * {
 *   "success": true,
 *   "message": "Transaction added successfully",
 *   "data": { ... },
 *   "timestamp": "2024-06-01T10:05:23"
 * }
 *
 * Example Error Response:
 * {
 *   "success": false,
 *   "message": "Transaction not found with ID: abc123",
 *   "data": null,
 *   "timestamp": "2024-06-01T10:05:23"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
