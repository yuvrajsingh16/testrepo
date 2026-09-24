package com.demo.oncall;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void processOrder_returnsSuccess_whenUserHasAddress() throws Exception {
        mockMvc.perform(post("/api/process-order")
                .param("userId", "1001")
                .param("product", "Laptop Pro 16")
                .param("quantity", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.shippingAddress").isNotEmpty());
    }

    @Test
    void processOrder_returnsError_whenUserIsMissingAddress() throws Exception {
        mockMvc.perform(post("/api/process-order")
                .param("userId", "1002")
                .param("product", "Laptop Pro 16")
                .param("quantity", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.error").value("Missing shipping address for user 1002"));
    }

    @Test
    void processOrder_returnsError_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/process-order")
                .param("userId", "9999")
                .param("product", "Laptop Pro 16")
                .param("quantity", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.error").value("User not found: 9999"));
    }
}
