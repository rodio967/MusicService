package com.example.provider.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Поднимает весь контекст */
@SpringBootTest(properties = "internal.api.secret=test-secret")
@AutoConfigureMockMvc
@Import(InternalSecurityIntegrationTest.EchoUserIdController.class)
class InternalSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void passesUserIdToControllerWhenSecretIsValid() throws Exception {
        mockMvc.perform(get("/test/user-id")
                        .header("X-Internal-Secret", "test-secret")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    void rejectsRequestWithoutSecret() throws Exception {
        mockMvc.perform(get("/test/user-id").header("X-User-Id", "42"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsRequestWithWrongSecret() throws Exception {
        mockMvc.perform(get("/test/user-id")
                        .header("X-Internal-Secret", "wrong")
                        .header("X-User-Id", "42"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMissingUserId() throws Exception {
        mockMvc.perform(get("/test/user-id").header("X-Internal-Secret", "test-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsNonNumericUserId() throws Exception {
        mockMvc.perform(get("/test/user-id")
                        .header("X-Internal-Secret", "test-secret")
                        .header("X-User-Id", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPathReturnsApiErrorResponse() throws Exception {
        mockMvc.perform(get("/api/unknown").header("X-Internal-Secret", "test-secret"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/unknown"));
    }

    @RestController
    static class EchoUserIdController {

        @GetMapping("/test/user-id")
        Long userId(@CurrentUserId Long userId) {
            return userId;
        }
    }
}
