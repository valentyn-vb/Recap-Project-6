package com.example.demo.session;

import com.example.demo.goal.Goal;
import com.example.demo.goal.GoalNotFoundException;
import com.example.demo.goal.GoalRepository;
import com.example.demo.session.dto.SessionRequest;
import com.example.demo.session.dto.SessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final GoalRepository goalRepository;

    public SessionService(SessionRepository sessionRepository, GoalRepository goalRepository) {
        this.sessionRepository = sessionRepository;
        this.goalRepository = goalRepository;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsForGoal(Long userId, Long goalId) {
        requireOwnedGoal(userId, goalId);
        return sessionRepository.findByGoal_IdAndGoal_User_IdOrderByDateDesc(goalId, userId).stream()
                .map(SessionService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long userId, Long goalId, Long sessionId) {
        requireOwnedGoal(userId, goalId);
        return toResponse(requireOwnedSession(userId, goalId, sessionId));
    }

    @Transactional
    public SessionResponse createSession(Long userId, Long goalId, SessionRequest request) {
        Goal goal = requireOwnedGoal(userId, goalId);
        LearningSession session = new LearningSession(
                goal, request.date(), request.durationMinutes(), request.notes(), request.tags());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse updateSession(Long userId, Long goalId, Long sessionId, SessionRequest request) {
        requireOwnedGoal(userId, goalId);
        LearningSession session = requireOwnedSession(userId, goalId, sessionId);
        session.update(request.date(), request.durationMinutes(), request.notes(), request.tags());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void deleteSession(Long userId, Long goalId, Long sessionId) {
        requireOwnedGoal(userId, goalId);
        LearningSession session = requireOwnedSession(userId, goalId, sessionId);
        sessionRepository.delete(session);
    }

    /** The goal must exist and belong to the authenticated user, otherwise 404. */
    private Goal requireOwnedGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(GoalNotFoundException::new);
    }

    /** The session must belong to the authenticated user and hang off the given goal. */
    private LearningSession requireOwnedSession(Long userId, Long goalId, Long sessionId) {
        return sessionRepository.findByIdAndGoal_User_Id(sessionId, userId)
                .filter(session -> Objects.equals(session.getGoal().getId(), goalId))
                .orElseThrow(SessionNotFoundException::new);
    }

    private static SessionResponse toResponse(LearningSession session) {
        return new SessionResponse(
                session.getId(),
                session.getGoal().getId(),
                session.getDate(),
                session.getDurationMinutes(),
                session.getNotes(),
                session.getTags());
    }
}
