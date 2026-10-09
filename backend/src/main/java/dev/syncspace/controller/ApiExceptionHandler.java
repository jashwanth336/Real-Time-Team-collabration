package dev.syncspace.controller;

import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String, Object> bad(RuntimeException e) {
    return error(e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, Object> denied(AccessDeniedException e) {
    return error(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
  Map<String, Object> validation(MethodArgumentNotValidException e) {
    var errors = new LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(x -> errors.put(x.getField(), x.getDefaultMessage()));
    return Map.of(
        "message",
        "Please correct the highlighted fields",
        "errors",
        errors,
        "timestamp",
        Instant.now().toString());
  }

  private Map<String, Object> error(String message) {
    return Map.of(
        "message",
        message == null ? "Request could not be completed" : message,
        "timestamp",
        Instant.now().toString());
  }
}
