package com.joao.adotec.dto.response;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        String message,
        T data
) {
    /**
     * Static factory method to create a standardized success response.
     * @param message A descriptive success message.
     * @param data The data payload to be returned.
     * @return An ApiResponse instance.
     * @param <T> The type of the data payload.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Instant.now(), message, data);
    }
}
