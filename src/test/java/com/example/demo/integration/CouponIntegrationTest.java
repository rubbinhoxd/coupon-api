package com.example.demo.integration;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class CouponIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateThenFindThenSoftDelete_couponFlow() throws Exception {
        // 1) CREATE (201)
        String createBody = """
        {
          "code": "ABC-123",
          "description": "Cupom de integração",
          "discountValue": 0.8,
          "expirationDate": "2026-11-04T17:14:45.180Z",
          "published": false
        }
        """;

        String createdJson = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(createdJson);
        String id = node.get("id").asText();
        assertThat(id).isNotBlank();

        // 2) FIND (200)
        mockMvc.perform(get("/coupon/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 3) SOFT DELETE (204)
        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isNoContent());

        // 4) DELETE AGAIN -> 409 (regra de negócio)
        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isConflict());

        // 5) FIND AFTER DELETE -> 404 (você trata DELETED como not found)
        mockMvc.perform(get("/coupon/{id}", id))
                .andExpect(status().isNotFound());
    }
}
