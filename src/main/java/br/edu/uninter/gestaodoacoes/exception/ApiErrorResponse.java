package br.edu.uninter.gestaodoacoes.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> detalhes
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, List.of());
    }

    public ApiErrorResponse(int status, String error, String message, List<String> detalhes) {
        this(LocalDateTime.now(), status, error, message, detalhes);
    }
}
