package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the id of the authenticated user from the security context.
 * Every service that reads or writes user-owned data must scope its
 * repository calls to this id — never to an id taken from the request.
 */
@Component
public class CurrentUserService {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return user.userId();
    }
}
