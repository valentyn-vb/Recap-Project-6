package com.example.demo.web;

import com.example.demo.goal.GoalNotFoundException;
import com.example.demo.profile.ProfileNotFoundException;
import com.example.demo.session.SessionNotFoundException;
import com.example.demo.user.EmailAlreadyInUseException;
import com.example.demo.user.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailAlreadyInUse(EmailAlreadyInUseException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidCredentialsException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProfileNotFound(ProfileNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(GoalNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleGoalNotFound(GoalNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleSessionNotFound(SessionNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException exception) {
        return Map.of("message", "Validation failed");
    }
}
