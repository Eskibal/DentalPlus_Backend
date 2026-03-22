package com.example.DentalPlus_Backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long userId;

    private static final String USER_JSON = """
        {
          "name": "Pepe",
          "surname": "Perez",
          "email": "pepe@test.com",
          "password": "1234Aa!"
        }
        """;

    @Test
    @Order(1)
    void createUser() throws Exception {
        String response = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        userId = json.get("id").asLong();

        assertNotNull(userId);
    }

    @Test
    @Order(2)
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void getUserById() throws Exception {
        mockMvc.perform(get("/user/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void loginSuccess() throws Exception {
        String body = """
            {
              "email": "pepe@test.com",
              "password": "1234Aa!"
            }
            """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void loginFail() throws Exception {
        String body = """
            {
              "email": "pepe@test.com",
              "password": "wrong123"
            }
            """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void updateUser() throws Exception {
        String body = """
            {
              "name": "PepeUpdated",
              "surname": "PerezUpdated",
              "email": "pepeupdated@test.com",
              "password": "Nueva123!"
            }
            """;

        mockMvc.perform(put("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/user/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void userShouldNotExist() throws Exception {
        mockMvc.perform(get("/user/" + userId))
                .andExpect(status().isNotFound());
    }
}