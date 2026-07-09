package com.example.demo.profile.dto;

import java.util.List;

public record ProfileResponse(Long id, String name, String cohort, List<String> focusAreas) {
}
