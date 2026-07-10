package com.example.demo.session;

import com.example.demo.goal.Goal;
import com.example.demo.goal.GoalNotFoundException;
import com.example.demo.goal.GoalRepository;
import com.example.demo.goal.GoalStatus;
import com.example.demo.session.dto.SessionRequest;
import com.example.demo.session.dto.SessionResponse;
import com.example.demo.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private GoalRepository goalRepository;

    private Goal ownedGoal(Long userId) {
        return new Goal(new User("alice@example.com", "hash"), "Learn Vue", null, GoalStatus.PLANNED);
    }

    @Test
    void createSessionRequiresOwnedGoal() {
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.empty());

        SessionService service = new SessionService(sessionRepository, goalRepository);

        assertThatThrownBy(() -> service.createSession(42L, 7L,
                new SessionRequest(LocalDate.of(2026, 1, 1), 60, "notes", List.of("vue"))))
                .isInstanceOf(GoalNotFoundException.class);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSessionPersistsAgainstOwnedGoal() {
        Goal goal = ownedGoal(42L);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(goal));
        when(sessionRepository.save(any(LearningSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionService service = new SessionService(sessionRepository, goalRepository);
        SessionResponse response = service.createSession(42L, 7L,
                new SessionRequest(LocalDate.of(2026, 1, 1), 90, "notes", List.of("vue", "spring")));

        ArgumentCaptor<LearningSession> captor = ArgumentCaptor.forClass(LearningSession.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getGoal()).isSameAs(goal);
        assertThat(captor.getValue().getDurationMinutes()).isEqualTo(90);
        assertThat(response.tags()).containsExactly("vue", "spring");
    }

    @Test
    void listSessionsScopedByGoalAndOwner() {
        Goal goal = ownedGoal(42L);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(goal));
        when(sessionRepository.findByGoal_IdAndGoal_User_IdOrderByDateDesc(7L, 42L))
                .thenReturn(List.of(new LearningSession(goal, LocalDate.of(2026, 1, 1), 60, null, List.of())));

        SessionService service = new SessionService(sessionRepository, goalRepository);
        List<SessionResponse> sessions = service.getSessionsForGoal(42L, 7L);

        assertThat(sessions).hasSize(1);
        verify(sessionRepository).findByGoal_IdAndGoal_User_IdOrderByDateDesc(7L, 42L);
        verify(sessionRepository, never()).findAll();
    }

    @Test
    void getSessionThrowsWhenNotOwned() {
        Goal goal = ownedGoal(42L);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(goal));
        when(sessionRepository.findByIdAndGoal_User_Id(5L, 42L)).thenReturn(Optional.empty());

        SessionService service = new SessionService(sessionRepository, goalRepository);

        assertThatThrownBy(() -> service.getSession(42L, 7L, 5L))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void updateSessionMutatesOwnSessionInPlace() {
        Goal goal = ownedGoal(42L);
        LearningSession existing = new LearningSession(goal, LocalDate.of(2026, 1, 1), 60, "old", List.of("a"));
        // A unit-constructed Goal has a null id; the session hangs off the same goal
        // instance, so Objects.equals(session.goal.id, goalId) holds when goalId == goal.getId().
        Long goalId = goal.getId();
        when(goalRepository.findByIdAndUser_Id(goalId, 42L)).thenReturn(Optional.of(goal));
        when(sessionRepository.findByIdAndGoal_User_Id(5L, 42L)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(any(LearningSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionService service = new SessionService(sessionRepository, goalRepository);
        SessionResponse response = service.updateSession(42L, goalId, 5L,
                new SessionRequest(LocalDate.of(2026, 2, 2), 120, "new", List.of("b")));

        verify(sessionRepository).save(existing);
        assertThat(existing.getDurationMinutes()).isEqualTo(120);
        assertThat(existing.getNotes()).isEqualTo("new");
        assertThat(existing.getTags()).containsExactly("b");
        assertThat(response.durationMinutes()).isEqualTo(120);
    }

    @Test
    void deleteSessionThrowsWhenNotOwned() {
        Goal goal = ownedGoal(42L);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(goal));
        when(sessionRepository.findByIdAndGoal_User_Id(5L, 42L)).thenReturn(Optional.empty());

        SessionService service = new SessionService(sessionRepository, goalRepository);

        assertThatThrownBy(() -> service.deleteSession(42L, 7L, 5L))
                .isInstanceOf(SessionNotFoundException.class);
        verify(sessionRepository, never()).delete(any());
    }
}
