package com.example.demo.controller;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponResponse;
import com.example.demo.enums.CouponStatusEnum;
import com.example.demo.exceptions.CouponAlreadyDeletedException;
import com.example.demo.exceptions.CouponNotFoundException;
import com.example.demo.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;


    @Test
    void shouldReturn201WhenCreateIsSuccessful() throws Exception {
        CouponDTO request = new CouponDTO();
        request.setCode("ABC123!!");
        request.setDescription("Cupom de teste");
        request.setDiscountValue(10.0);
        request.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setPublished(false);

        UUID id = UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50");

        CouponResponse response = new CouponResponse();
        response.setId(id);
        response.setCode("ABC123");
        response.setDescription(request.getDescription());
        response.setDiscountValue(request.getDiscountValue());
        response.setExpirationDate(request.getExpirationDate());
        response.setPublished(false);
        response.setStatus(CouponStatusEnum.ACTIVE);
        response.setRedeemed(false);

        when(couponService.create(any(CouponDTO.class))).thenReturn(response);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.code", is("ABC123")))
                .andExpect(jsonPath("$.description", is("Cupom de teste")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.redeemed", is(false)));
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        CouponDTO request = new CouponDTO();
        request.setCode("ABC123");
        request.setDescription("");
        request.setDiscountValue((0.4));
        request.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturn204WhenDeleteIsSuccessful() throws Exception {
        mockMvc.perform(delete("/coupon/{id}", UUID.fromString("55e5847f-9971-4d84-8921-c0186bc006e1")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenCouponNotFoundOnDelete() throws Exception {
        doThrow(new CouponNotFoundException("not found"))
                .when(couponService).delete(UUID.fromString("55e5847f-9971-4d84-8921-c0186bc006e1"));

        mockMvc.perform(delete("/coupon/{id}", UUID.fromString("55e5847f-9971-4d84-8921-c0186bc006e1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenCouponAlreadyDeletedOnDelete() throws Exception {
        doThrow(new CouponAlreadyDeletedException("already deleted"))
                .when(couponService).delete(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));

        mockMvc.perform(delete("/coupon/{id}", UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50")))
                .andExpect(status().isConflict());
    }

}
