package com.example.demo.session;

import com.example.demo.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainer.class)
@ActiveProfiles("test")
@Transactional
class SessionControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    SessionControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String registerAndGetToken(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"s3cret-password\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private Long createGoal(String token) throws Exception {
        String body = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Learn Vue\",\"description\":\"d\",\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createSession(String token, Long goalId) throws Exception {
        String body = mockMvc.perform(post("/api/goals/" + goalId + "/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-01-15\",\"durationMinutes\":60,\"notes\":\"n\",\"tags\":[\"vue\"]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void userCanManageSessionsUnderOwnGoal() throws Exception {
        String token = registerAndGetToken("alice@example.com");
        Long goalId = createGoal(token);

        Long sessionId = createSession(token, goalId);

        mockMvc.perform(get("/api/goals/" + goalId + "/sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].durationMinutes").value(60))
                .andExpect(jsonPath("$[0].tags[0]").value("vue"))
                .andExpect(jsonPath("$[0].goalId").value(goalId));

        mockMvc.perform(put("/api/goals/" + goalId + "/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-02-20\",\"durationMinutes\":90,\"notes\":\"more\",\"tags\":[\"spring\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(90))
                .andExpect(jsonPath("$.tags[0]").value("spring"));

        mockMvc.perform(delete("/api/goals/" + goalId + "/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/goals/" + goalId + "/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidSessionPayloadIsRejected() throws Exception {
        String token = registerAndGetToken("alice@example.com");
        Long goalId = createGoal(token);

        // missing date and non-positive duration
        mockMvc.perform(post("/api/goals/" + goalId + "/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":null,\"durationMinutes\":0,\"notes\":\"n\",\"tags\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatingSessionUnderAnothersGoalIsNotFound() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");
        String tokenB = registerAndGetToken("bob@example.com");
        Long aliceGoal = createGoal(tokenA);

        // B cannot create a session under A's goal
        mockMvc.perform(post("/api/goals/" + aliceGoal + "/sessions")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-01-15\",\"durationMinutes\":60,\"notes\":\"n\",\"tags\":[]}"))
                .andExpect(status().isNotFound());

        // B cannot list A's goal's sessions
        mockMvc.perform(get("/api/goals/" + aliceGoal + "/sessions").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void sessionsAreIsolatedBetweenUsers() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");
        String tokenB = registerAndGetToken("bob@example.com");
        Long aliceGoal = createGoal(tokenA);
        Long aliceSession = createSession(tokenA, aliceGoal);
        Long bobGoal = createGoal(tokenB);

        // B cannot read/update/delete A's session even via B's own goal path
        mockMvc.perform(get("/api/goals/" + bobGoal + "/sessions/" + aliceSession)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/goals/" + aliceGoal + "/sessions/" + aliceSession)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-03-01\",\"durationMinutes\":10,\"notes\":\"x\",\"tags\":[]}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/goals/" + aliceGoal + "/sessions/" + aliceSession)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // A's session is untouched
        mockMvc.perform(get("/api/goals/" + aliceGoal + "/sessions/" + aliceSession)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    void sessionEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/goals/1/sessions"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/goals/1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-01-15\",\"durationMinutes\":60,\"notes\":null,\"tags\":[]}"))
                .andExpect(status().isUnauthorized());
    }
}
