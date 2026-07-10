package com.example.demo.goal;

import com.example.demo.support.PostgresTestContainer;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PostgresTestContainer.class)
class GoalRepositoryTest {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    GoalRepositoryTest(@Autowired GoalRepository goalRepository, @Autowired UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Test
    void savesAndFindsGoalsScopedToOwner() {
        User alice = userRepository.save(new User("alice@example.com", "hash"));
        User bob = userRepository.save(new User("bob@example.com", "hash"));
        goalRepository.save(new Goal(alice, "Learn Vue", "Router + Pinia", GoalStatus.PLANNED));
        goalRepository.save(new Goal(bob, "Learn Spring", null, GoalStatus.IN_PROGRESS));

        List<Goal> aliceGoals = goalRepository.findByUser_IdOrderByCreatedAtDesc(alice.getId());

        assertThat(aliceGoals).hasSize(1);
        assertThat(aliceGoals.get(0).getTitle()).isEqualTo("Learn Vue");
        assertThat(aliceGoals.get(0).getStatus()).isEqualTo(GoalStatus.PLANNED);
    }

    @Test
    void findByIdAndUserIdReturnsOwnGoalOnly() {
        User alice = userRepository.save(new User("alice@example.com", "hash"));
        User bob = userRepository.save(new User("bob@example.com", "hash"));
        Goal aliceGoal = goalRepository.save(new Goal(alice, "Learn Vue", null, GoalStatus.PLANNED));

        assertThat(goalRepository.findByIdAndUser_Id(aliceGoal.getId(), alice.getId())).isPresent();
        // Bob must not be able to load Alice's goal by id.
        assertThat(goalRepository.findByIdAndUser_Id(aliceGoal.getId(), bob.getId())).isEmpty();
    }

    @Test
    void returnsEmptyListForUserWithoutGoals() {
        User carol = userRepository.save(new User("carol@example.com", "hash"));

        Optional<Goal> none = goalRepository.findByIdAndUser_Id(999L, carol.getId());

        assertThat(goalRepository.findByUser_IdOrderByCreatedAtDesc(carol.getId())).isEmpty();
        assertThat(none).isEmpty();
    }
}
