package com.example.demo.goal;

import com.example.demo.goal.dto.GoalRequest;
import com.example.demo.goal.dto.GoalResponse;
import com.example.demo.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // The user id only ever comes from the verified token's principal —
    // never from a path variable, query param, or request body.
    @GetMapping
    public List<GoalResponse> listGoals(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return goalService.getGoalsForUser(currentUser.userId());
    }

    @GetMapping("/{id}")
    public GoalResponse getGoal(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return goalService.getGoalForUser(currentUser.userId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse createGoal(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody GoalRequest request) {
        return goalService.createGoal(currentUser.userId(), request);
    }

    @PutMapping("/{id}")
    public GoalResponse updateGoal(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request) {
        return goalService.updateGoal(currentUser.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        goalService.deleteGoal(currentUser.userId(), id);
    }
}
