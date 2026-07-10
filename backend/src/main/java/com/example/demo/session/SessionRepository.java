package com.example.demo.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<LearningSession, Long> {

    List<LearningSession> findByGoal_IdAndGoal_User_IdOrderByDateDesc(Long goalId, Long userId);

    Optional<LearningSession> findByIdAndGoal_User_Id(Long id, Long userId);
}
