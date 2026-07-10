package com.example.demo.session;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException() {
        super("No learning session with that id exists for the current user");
    }
}
