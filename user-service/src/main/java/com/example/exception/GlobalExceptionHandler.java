package com.example.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found: {}", ex.getMessage(), ex);
        return buildError(ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage(), ex);
        return buildError(ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage(), ex);
        return buildError(ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage illegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Illegal state: {}", ex.getMessage(), ex);
        return buildError(ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred", ex);
        return buildError("Unexpected error occurred", request);
    }

    @ExceptionHandler({JwtException.class, BadCredentialsException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBadAuth(Exception ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage(), ex);
        return buildError("Invalid username or password", request);
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleEntityExists(EntityExistsException ex) {
        log.warn("Found duplicated entity: {}", ex.getMessage(), ex);
        return new ErrorMessage(Instant.now().toString(),"Entity already exists: " + ex.getMessage(), "");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildError("Validation failed", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleConflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Found duplicated entity: {}", ex.getMessage());
        return buildError("Entity already exist", request);
    }

    private ErrorMessage buildError(String message, HttpServletRequest request) {
        return new ErrorMessage(
                Instant.now().toString(),
                message,
                request.getRequestURI()
        );
    }
}
