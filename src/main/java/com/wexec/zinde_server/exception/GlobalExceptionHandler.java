package com.wexec.zinde_server.exception;

import com.wexec.zinde_server.dto.response.ApiError;
import com.wexec.zinde_server.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(ApiResponse.error(
                ApiError.builder()
                        .code("VALIDATION_ERROR")
                        .message("Girilen bilgilerde hata var.")
                        .fieldErrors(violations)
                        .build()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "Bir hata oluştu. Lütfen tekrar deneyin."));
    }
}
