package com.joao.adotec.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Resposta padronizada da API")
public record ApiResponse<T>(
        @Schema(description = "Timestamp do momento em que a resposta foi gerada", example = "2026-06-08T18:54:00Z")
        Instant timestamp,
        
        @Schema(description = "Mensagem explicativa sobre o status da resposta", example = "Operation completed successfully")
        String message,
        
        @Schema(description = "Carga útil (payload) de retorno com os dados solicitados")
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
