package com.placement.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.placement.dto.Result;
import com.placement.util.ApiMessages;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.notFound(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String details = e.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> ApiMessages.get("api.error.validationField", fieldError.getField(), fieldError.getDefaultMessage()))
            .distinct()
            .collect(Collectors.joining(ApiMessages.get("api.error.validationSeparator")));
        return ResponseEntity.badRequest()
            .body(Result.badRequest(ApiMessages.get("api.error.validationPrefix") + " " + details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationExceptions(ConstraintViolationException e) {
        String details = e.getConstraintViolations().stream()
            .map(violation -> ApiMessages.get("api.error.validationField", violation.getPropertyPath(), violation.getMessage()))
            .distinct()
            .collect(Collectors.joining(ApiMessages.get("api.error.validationSeparator")));
        return ResponseEntity.badRequest()
            .body(Result.badRequest(ApiMessages.get("api.error.validationPrefix") + " " + details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(Result.badRequest(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> unauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Result.unauthorized(e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatusException(ResponseStatusException e) {
        HttpStatusCode status = e.getStatusCode();
        String message = e.getReason();
        return ResponseEntity.status(status)
            .body(Result.error(status.value(), message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Result.error(HttpStatus.CONFLICT.value(), ApiMessages.get("api.error.dataConflict")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> internalError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.internalError(null));
    }
}
