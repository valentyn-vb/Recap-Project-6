package com.example.demo.session;

import com.example.demo.goal.Goal;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_sessions")
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 2000)
    private String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_tags", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    protected LearningSession() {
        // JPA
    }

    public LearningSession(Goal goal, LocalDate date, Integer durationMinutes, String notes, List<String> tags) {
        this.goal = goal;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public void update(LocalDate date, Integer durationMinutes, String notes, List<String> tags) {
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public Long getId() {
        return id;
    }

    public Goal getGoal() {
        return goal;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getTags() {
        return List.copyOf(tags);
    }
}
