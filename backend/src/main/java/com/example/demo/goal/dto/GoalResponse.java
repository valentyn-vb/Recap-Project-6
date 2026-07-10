package com.example.demo.goal.dto;

import com.example.demo.goal.GoalStatus;

import java.time.Instant;

public record GoalResponse(
        Long id,
        String title,
        String description,
        GoalStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
