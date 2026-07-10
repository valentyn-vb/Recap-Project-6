package com.example.demo.goal;

import com.example.demo.goal.dto.GoalRequest;
import com.example.demo.goal.dto.GoalResponse;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void listsGoalsScopedToPrincipalOnly() {
        User alice = new User("alice@example.com", "hash");
        when(goalRepository.findByUser_IdOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(new Goal(alice, "Learn Vue", "notes", GoalStatus.PLANNED)));

        GoalService service = new GoalService(goalRepository, userRepository);
        List<GoalResponse> goals = service.getGoalsForUser(42L);

        assertThat(goals).hasSize(1);
        assertThat(goals.get(0).title()).isEqualTo("Learn Vue");

        // guardrail: query scoped to the passed-in principal id, never a global read
        verify(goalRepository).findByUser_IdOrderByCreatedAtDesc(eq(42L));
        verify(goalRepository, never()).findAll();
    }

    @Test
    void getGoalThrowsWhenNotOwnedByUser() {
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.empty());

        GoalService service = new GoalService(goalRepository, userRepository);

        assertThatThrownBy(() -> service.getGoalForUser(42L, 7L))
                .isInstanceOf(GoalNotFoundException.class);
        verify(goalRepository).findByIdAndUser_Id(7L, 42L);
        verify(goalRepository, never()).findById(any());
    }

    @Test
    void createGoalPersistsAgainstAuthenticatedUser() {
        User alice = new User("alice@example.com", "hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(alice));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalService service = new GoalService(goalRepository, userRepository);
        GoalResponse response = service.createGoal(42L,
                new GoalRequest("Learn Vue", "Router + Pinia", GoalStatus.IN_PROGRESS));

        ArgumentCaptor<Goal> captor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(alice);
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
        assertThat(response.title()).isEqualTo("Learn Vue");
    }

    @Test
    void createGoalDefaultsStatusToPlannedWhenMissing() {
        User alice = new User("alice@example.com", "hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(alice));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalService service = new GoalService(goalRepository, userRepository);
        GoalResponse response = service.createGoal(42L, new GoalRequest("Learn Vue", null, null));

        assertThat(response.status()).isEqualTo(GoalStatus.PLANNED);
    }

    @Test
    void updateGoalMutatesOwnGoalInPlace() {
        User alice = new User("alice@example.com", "hash");
        Goal existing = new Goal(alice, "Old", "old desc", GoalStatus.PLANNED);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalService service = new GoalService(goalRepository, userRepository);
        GoalResponse response = service.updateGoal(42L, 7L,
                new GoalRequest("New", "new desc", GoalStatus.DONE));

        verify(goalRepository).save(existing);
        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(existing.getStatus()).isEqualTo(GoalStatus.DONE);
        assertThat(response.title()).isEqualTo("New");
    }

    @Test
    void updateGoalThrowsWhenNotOwned() {
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.empty());

        GoalService service = new GoalService(goalRepository, userRepository);

        assertThatThrownBy(() -> service.updateGoal(42L, 7L,
                new GoalRequest("New", null, GoalStatus.DONE)))
                .isInstanceOf(GoalNotFoundException.class);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void deleteGoalRemovesOwnGoal() {
        User alice = new User("alice@example.com", "hash");
        Goal existing = new Goal(alice, "Learn Vue", null, GoalStatus.PLANNED);
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.of(existing));

        GoalService service = new GoalService(goalRepository, userRepository);
        service.deleteGoal(42L, 7L);

        verify(goalRepository).delete(existing);
    }

    @Test
    void deleteGoalThrowsWhenNotOwned() {
        when(goalRepository.findByIdAndUser_Id(7L, 42L)).thenReturn(Optional.empty());

        GoalService service = new GoalService(goalRepository, userRepository);

        assertThatThrownBy(() -> service.deleteGoal(42L, 7L))
                .isInstanceOf(GoalNotFoundException.class);
        verify(goalRepository, never()).delete(any());
    }
}
