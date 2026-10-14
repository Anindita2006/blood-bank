package com.bloodbank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Spring Boot testing: starts the whole app in test mode.
// MockMvc lets us "call" the API without a real browser or network.
@SpringBootTest
@AutoConfigureMockMvc
class BloodBankTests {

    @Autowired BloodService service;
    @Autowired MockMvc mvc;

    // ---------- Unit tests on the business logic ----------

    @Test
    void oNegativeCanOnlyReceiveONegative() {
        assertEquals(java.util.List.of("O-"), service.compatibleDonors("O-"));
    }

    @Test
    void abPositiveCanReceiveFromAllEightGroups() {
        assertEquals(8, service.compatibleDonors("AB+").size());
    }

    @Test
    void invalidGroupThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> service.compatibleDonors("X+"));
    }

    @Test
    void negativeUnitsAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> service.update(new Stock("Test Bank", "Pune", "A+", -2)));
    }

    // ---------- API tests using MockMvc ----------

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mvc.perform(get("/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void searchForONegativeInHyderabadReturnsRedCross() throws Exception {
        mvc.perform(get("/api/search").param("city", "Hyderabad").param("group", "O-"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].bank").value("Red Cross"))
           .andExpect(jsonPath("$[0].low").value(true));   // only 3 units => low
    }

    @Test
    void invalidGroupGives400() throws Exception {
        mvc.perform(get("/api/search").param("city", "Hyderabad").param("group", "Z"))
           .andExpect(status().isBadRequest());
    }
}
