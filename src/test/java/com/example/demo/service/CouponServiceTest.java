package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponResponse;
import com.example.demo.entities.CouponEntity;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.CouponAlreadyDeletedException;
import com.example.demo.exceptions.CouponNotFoundException;
import com.example.demo.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private CouponDTO couponDTO;

    @BeforeEach
    void setup() {
        couponDTO = new CouponDTO();
        couponDTO.setCode("ABC123!!");
        couponDTO.setDescription("Cupom de teste");
        couponDTO.setDiscountValue(10.0);
        couponDTO.setExpirationDate(LocalDateTime.now().plusDays(1));
        couponDTO.setPublished(true);
    }

    @Test
    void shouldCreateCouponWhenDataIsValid() {
        // arrange
        CouponEntity saved = new CouponEntity();
        saved.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        saved.setCode("ABC123"); // esperado após sanitize
        saved.setDescription(couponDTO.getDescription());
        saved.setDiscountValue(couponDTO.getDiscountValue());
        saved.setExpirationDate(couponDTO.getExpirationDate());
        saved.setPublished(true);

        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);

        // act
        CouponResponse response = couponService.create(couponDTO);

        // assert
        assertThat(response.getId()).isEqualTo(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        assertThat(response.getCode()).isEqualTo("ABC123"); // sanitizado
        assertThat(response.getDescription()).isEqualTo(couponDTO.getDescription());
        assertThat(response.getDiscountValue()).isEqualTo(couponDTO.getDiscountValue());
        assertThat(response.getExpirationDate()).isEqualTo(couponDTO.getExpirationDate());
        assertThat(response.getPublished()).isTrue();

        // garante que o objeto salvo também foi sanitizado
        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("ABC123");
    }

    @Test
    void shouldFailWhenDiscountValueIsLessThanMin() {
        couponDTO.setDiscountValue(0.4);

        assertThatThrownBy(() -> couponService.create(couponDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("discount"); // ajuste a msg se quiser mais específico

        verifyNoInteractions(couponRepository);
    }

    @Test
    void shouldFailWhenExpirationDateIsInThePast() {
        couponDTO.setExpirationDate(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> couponService.create(couponDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expiration");

        verifyNoInteractions(couponRepository);
    }

    @Test
    void shouldFailWhenCodeAfterSanitizeHasLessThanSixCharacters() {
        couponDTO.setCode("A!1"); // vai virar "A1"

        assertThatThrownBy(() -> couponService.create(couponDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("6 alphanumeric");

        verifyNoInteractions(couponRepository);
    }

    @Test
    void shouldDeleteCouponWhenNotDeletedYet() {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        coupon.setCode("ABC123");
        coupon.setDeleted(null);

        when(couponRepository.findById(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"))).thenReturn(Optional.of(coupon));

        // act
        couponService.delete(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));

        // assert
        assertThat(coupon.getDeleted()).isNotNull();
        verify(couponRepository).save(coupon);
    }


    @Test
    void shouldThrowWhenCouponNotFoundOnDelete() {
        when(couponRepository.findById(UUID.fromString("55e5847f-9971-4d84-8921-c0186bc006e1"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.delete(UUID.fromString("55e5847f-9971-4d84-8921-c0186bc006e1")))
                .isInstanceOf(CouponNotFoundException.class);

        verify(couponRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCouponAlreadyDeleted() {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        coupon.setCode("ABC123");
        coupon.setDeleted(true);

        when(couponRepository.findById(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"))).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.delete(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50")))
                .isInstanceOf(CouponAlreadyDeletedException.class);

        verify(couponRepository, never()).save(any());
    }

}
