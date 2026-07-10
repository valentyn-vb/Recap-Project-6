package com.example.demo.goal.dto;

import com.example.demo.goal.GoalStatus;
import jakarta.validation.constraints.NotBlank;

public record GoalRequest(
        @NotBlank String title,
        String description,
        GoalStatus status) {
}
