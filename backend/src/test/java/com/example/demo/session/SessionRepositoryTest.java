package com.example.demo.session;

import com.example.demo.goal.Goal;
import com.example.demo.goal.GoalRepository;
import com.example.demo.goal.GoalStatus;
import com.example.demo.support.PostgresTestContainer;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PostgresTestContainer.class)
class SessionRepositoryTest {

    private final SessionRepository sessionRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    SessionRepositoryTest(
            @Autowired SessionRepository sessionRepository,
            @Autowired GoalRepository goalRepository,
            @Autowired UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Test
    void findsSessionsScopedByGoalAndOwner() {
        User alice = userRepository.save(new User("alice@example.com", "hash"));
        User bob = userRepository.save(new User("bob@example.com", "hash"));
        Goal aliceGoal = goalRepository.save(new Goal(alice, "Learn Vue", null, GoalStatus.PLANNED));
        Goal bobGoal = goalRepository.save(new Goal(bob, "Learn Spring", null, GoalStatus.PLANNED));
        sessionRepository.save(new LearningSession(aliceGoal, LocalDate.of(2026, 1, 1), 60, "notes", List.of("vue")));
        sessionRepository.save(new LearningSession(bobGoal, LocalDate.of(2026, 1, 2), 30, null, List.of()));

        List<LearningSession> aliceSessions =
                sessionRepository.findByGoal_IdAndGoal_User_IdOrderByDateDesc(aliceGoal.getId(), alice.getId());

        assertThat(aliceSessions).hasSize(1);
        assertThat(aliceSessions.get(0).getDurationMinutes()).isEqualTo(60);
        assertThat(aliceSessions.get(0).getTags()).containsExactly("vue");

        // Alice cannot reach her session through Bob's goal id, nor Bob through hers
        assertThat(sessionRepository.findByGoal_IdAndGoal_User_IdOrderByDateDesc(bobGoal.getId(), alice.getId()))
                .isEmpty();
    }

    @Test
    void findByIdAndOwnerReturnsOwnSessionOnly() {
        User alice = userRepository.save(new User("alice@example.com", "hash"));
        User bob = userRepository.save(new User("bob@example.com", "hash"));
        Goal aliceGoal = goalRepository.save(new Goal(alice, "Learn Vue", null, GoalStatus.PLANNED));
        LearningSession session =
                sessionRepository.save(new LearningSession(aliceGoal, LocalDate.of(2026, 1, 1), 60, null, List.of()));

        assertThat(sessionRepository.findByIdAndGoal_User_Id(session.getId(), alice.getId())).isPresent();
        assertThat(sessionRepository.findByIdAndGoal_User_Id(session.getId(), bob.getId())).isEmpty();
    }
}
