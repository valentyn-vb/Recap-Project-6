package com.example.demo.profile;

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
class ProfileControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    ProfileControllerTest(@Autowired MockMvc mockMvc) {
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

    @Test
    void userCanCreateAndReadOwnProfile() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"cohort\":\"NF-2026\",\"focusAreas\":[\"vue\",\"spring\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.cohort").value("NF-2026"))
                .andExpect(jsonPath("$.focusAreas[0]").value("vue"))
                .andExpect(jsonPath("$.focusAreas[1]").value("spring"));

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void userWithoutProfileGetsNotFoundNotSomeoneElsesData() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");
        String tokenB = registerAndGetToken("bob@example.com");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"cohort\":\"NF-2026\",\"focusAreas\":[\"vue\"]}"))
                .andExpect(status().isOk());

        // B must NOT see A's profile — 404, no fallback, no leak
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void writesAreIsolatedBetweenUsers() throws Exception {
        String tokenA = registerAndGetToken("alice@example.com");
        String tokenB = registerAndGetToken("bob@example.com");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"cohort\":\"NF-2026\",\"focusAreas\":[\"vue\"]}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bob\",\"cohort\":\"NF-2027\",\"focusAreas\":[\"java\"]}"))
                .andExpect(status().isOk());

        // A's profile is untouched by B's write
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.cohort").value("NF-2026"))
                .andExpect(jsonPath("$.focusAreas[0]").value("vue"));
    }

    @Test
    void profileEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nobody\",\"cohort\":null,\"focusAreas\":[]}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }
}
