package com.example.demo.session.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record SessionRequest(
        @NotNull LocalDate date,
        @NotNull @Positive Integer durationMinutes,
        String notes,
        List<String> tags) {

    public SessionRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
