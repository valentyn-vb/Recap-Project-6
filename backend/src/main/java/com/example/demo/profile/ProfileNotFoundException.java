package com.example.demo.profile;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException() {
        super("No profile exists for the current user");
    }
}
