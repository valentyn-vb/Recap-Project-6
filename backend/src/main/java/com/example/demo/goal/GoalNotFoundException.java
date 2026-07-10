package com.example.demo.goal;

public class GoalNotFoundException extends RuntimeException {

    public GoalNotFoundException() {
        super("No goal with that id exists for the current user");
    }
}
