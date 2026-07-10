package com.example.demo.goal;

import com.example.demo.goal.dto.GoalRequest;
import com.example.demo.goal.dto.GoalResponse;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsForUser(Long userId) {
        return goalRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(GoalService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoalForUser(Long userId, Long goalId) {
        return goalRepository.findByIdAndUser_Id(goalId, userId)
                .map(GoalService::toResponse)
                .orElseThrow(GoalNotFoundException::new);
    }

    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user " + userId + " no longer exists"));
        Goal goal = new Goal(user, request.title(), request.description(), request.status());
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request) {
        Goal goal = goalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(GoalNotFoundException::new);
        goal.update(request.title(), request.description(), request.status());
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(GoalNotFoundException::new);
        goalRepository.delete(goal);
    }

    private static GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt());
    }
}
