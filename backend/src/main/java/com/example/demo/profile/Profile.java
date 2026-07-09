package com.example.demo.profile;

import com.example.demo.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    private String cohort;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_focus_areas", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "focus_area")
    private List<String> focusAreas = new ArrayList<>();

    protected Profile() {
        // JPA
    }

    public Profile(User user, String name, String cohort, List<String> focusAreas) {
        this.user = user;
        this.name = name;
        this.cohort = cohort;
        this.focusAreas = new ArrayList<>(focusAreas);
    }

    public void update(String name, String cohort, List<String> focusAreas) {
        this.name = name;
        this.cohort = cohort;
        this.focusAreas = new ArrayList<>(focusAreas);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getCohort() {
        return cohort;
    }

    public List<String> getFocusAreas() {
        return List.copyOf(focusAreas);
    }
}
