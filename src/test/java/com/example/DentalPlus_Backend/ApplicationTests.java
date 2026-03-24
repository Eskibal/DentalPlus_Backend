package com.example.DentalPlus_Backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long userId;
    private static Long patientId;

    private static final String CREATE_USER_JSON = """
        {
          "name": "Pepe",
          "surname": "Perez",
          "email": "pepe@test.com",
          "password": "1234Aa!"
        }
        """;

    private static final String LOGIN_USER_JSON = """
        {
          "email": "pepe@test.com",
          "password": "1234Aa!"
        }
        """;

    private static final String LOGIN_USER_WRONG_PASSWORD_JSON = """
        {
          "email": "pepe@test.com",
          "password": "wrong123"
        }
        """;

    private static final String UPDATE_USER_JSON = """
        {
          "name": "PepeUpdated",
          "surname": "PerezUpdated",
          "email": "pepeupdated@test.com",
          "password": "Nueva123!"
        }
        """;

    private static final String LOGIN_UPDATED_USER_JSON = """
        {
          "email": "pepeupdated@test.com",
          "password": "Nueva123!"
        }
        """;

    private static final String CREATE_PATIENT_FROM_USER_JSON = """
        {
          "nationalId": "12345678A",
          "phone": "600123456",
          "birthDate": "1995-05-10",
          "gender": "Male",
          "address": "Street 123",
          "city": "Barcelona",
          "consultationReason": "Tooth pain"
        }
        """;

    private static final String UPDATE_PATIENT_JSON = """
        {
          "nationalId": "12345678A",
          "phone": "699999999",
          "birthDate": "1995-05-10",
          "gender": "Male",
          "address": "Updated Street 999",
          "city": "Valencia",
          "consultationReason": "Updated reason"
        }
        """;

    @Test
    @Order(1)
    void createUser() throws Exception {
        String response = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_USER_JSON))
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
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_USER_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void loginFail() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_USER_WRONG_PASSWORD_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void updateUser() throws Exception {
        mockMvc.perform(put("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_USER_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void loginAfterUpdate() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_UPDATED_USER_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void createPatientFromExistingUser() throws Exception {
        String response = mockMvc.perform(post("/patient/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PATIENT_FROM_USER_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        patientId = json.get("id").asLong();

        assertNotNull(patientId);
        assertEquals(userId, patientId);
    }

    @Test
    @Order(9)
    void getAllPatients() throws Exception {
        mockMvc.perform(get("/patient"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void getPatientById() throws Exception {
        mockMvc.perform(get("/patient/" + patientId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    void updatePatient() throws Exception {
        mockMvc.perform(put("/patient/" + patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_PATIENT_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    void deletePatient() throws Exception {
        mockMvc.perform(delete("/patient/" + patientId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void patientShouldNotExist() throws Exception {
        mockMvc.perform(get("/patient/" + patientId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/user/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(15)
    void userShouldNotExist() throws Exception {
        mockMvc.perform(get("/user/" + userId))
                .andExpect(status().isNotFound());
    }
}