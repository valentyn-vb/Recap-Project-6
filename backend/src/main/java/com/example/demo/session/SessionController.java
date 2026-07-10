package com.example.demo.session;

import com.example.demo.security.AuthenticatedUser;
import com.example.demo.session.dto.SessionRequest;
import com.example.demo.session.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals/{goalId}/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // The user id only ever comes from the verified token's principal — never from
    // the path. The goalId path variable is always resolved together with that
    // principal, so a user can only reach sessions under goals they own.
    @GetMapping
    public List<SessionResponse> listSessions(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long goalId) {
        return sessionService.getSessionsForGoal(currentUser.userId(), goalId);
    }

    @GetMapping("/{id}")
    public SessionResponse getSession(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long goalId,
            @PathVariable Long id) {
        return sessionService.getSession(currentUser.userId(), goalId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse createSession(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long goalId,
            @Valid @RequestBody SessionRequest request) {
        return sessionService.createSession(currentUser.userId(), goalId, request);
    }

    @PutMapping("/{id}")
    public SessionResponse updateSession(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long goalId,
            @PathVariable Long id,
            @Valid @RequestBody SessionRequest request) {
        return sessionService.updateSession(currentUser.userId(), goalId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long goalId,
            @PathVariable Long id) {
        sessionService.deleteSession(currentUser.userId(), goalId, id);
    }
}
