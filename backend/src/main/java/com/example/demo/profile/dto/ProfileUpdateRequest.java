package com.example.demo.profile.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProfileUpdateRequest(
        @NotBlank String name,
        String cohort,
        List<String> focusAreas) {

    public ProfileUpdateRequest {
        focusAreas = focusAreas == null ? List.of() : List.copyOf(focusAreas);
    }
}
