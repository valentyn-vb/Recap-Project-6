package com.example.demo.goal;

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
class GoalControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    GoalControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    /** Registers a user through the real auth endpoint and returns a real JWT. */
    private String registerAndGetToken(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"s3cret-password\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private Long createGoal(String token, String title, String status) throws Exception {
        String body = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"description\":\"desc\",\"status\":\"" + status + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void userCanCreateListReadUpdateAndDeleteOwnGoals() throws Exception {
        String token = registerAndGetToken("alice@example.com");

        Long id = createGoal(token, "Learn Vue", "PLANNED");

        mockMvc.perform(get("/api/goals").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Learn Vue"))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

        mockMvc.perform(get("/api/goals/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn Vue"));

        mockMvc.perform(put("/api/goals/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Learn Vue 3\",\"description\":\"desc\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn Vue 3"))
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(delete("/api/goals/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/goals/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidGoalPayloadIsRejected() throws Exception {
        String token = registerAndGetToken("alice@example.com");

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"desc\",\"status\":\"PLANNED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void goalsAreIsolatedBetweenUsers() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");
        String tokenB = registerAndGetToken("bob@example.com");

        Long aliceGoal = createGoal(tokenA, "Alice Goal", "PLANNED");

        // B's list must not contain A's goal
        mockMvc.perform(get("/api/goals").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // B must not read, update, or delete A's goal — all 404, no leak
        mockMvc.perform(get("/api/goals/" + aliceGoal).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/goals/" + aliceGoal)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hijack\",\"description\":\"x\",\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/goals/" + aliceGoal).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // A's goal is untouched
        mockMvc.perform(get("/api/goals/" + aliceGoal).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alice Goal"));
    }

    @Test
    void goalEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"description\":null,\"status\":\"PLANNED\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/goals/1").header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }
}
