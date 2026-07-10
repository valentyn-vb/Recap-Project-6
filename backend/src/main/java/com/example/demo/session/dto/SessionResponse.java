package com.example.demo.session.dto;

import java.time.LocalDate;
import java.util.List;

public record SessionResponse(
        Long id,
        Long goalId,
        LocalDate date,
        Integer durationMinutes,
        String notes,
        List<String> tags) {
}
