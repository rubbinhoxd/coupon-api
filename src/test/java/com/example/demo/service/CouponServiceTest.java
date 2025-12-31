package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponResponse;
import com.example.demo.entities.CouponEntity;
import com.example.demo.enums.CouponStatusEnum;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        couponDTO.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));
        couponDTO.setPublished(false);
    }

    @Test
    void shouldCreateCouponWhenDataIsValid() {
        CouponEntity saved = new CouponEntity();
        saved.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        saved.setCode("ABC123");
        saved.setDescription(couponDTO.getDescription());
        saved.setDiscountValue(couponDTO.getDiscountValue());
        saved.setExpirationDate(couponDTO.getExpirationDate());
        saved.setStatus(CouponStatusEnum.ACTIVE);
        saved.setPublished(false);
        saved.setRedeemed(false);

        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);


        CouponResponse response = couponService.create(couponDTO);

        assertThat(response.getId()).isEqualTo(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        assertThat(response.getCode()).isEqualTo("ABC123");
        assertThat(response.getDescription()).isEqualTo(couponDTO.getDescription());
        assertThat(response.getDiscountValue()).isEqualTo(couponDTO.getDiscountValue());
        assertThat(response.getExpirationDate()).isEqualTo(couponDTO.getExpirationDate());
        assertThat(response.getPublished()).isFalse();
        assertThat(response.getStatus()).isEqualTo(CouponStatusEnum.ACTIVE);
        assertThat(response.getRedeemed()).isFalse();


        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("ABC123");
    }

    @Test
    void shouldCreateCouponAsPublishedWhenPublishedIsTrue() {
        couponDTO.setPublished(true);

        CouponEntity saved = new CouponEntity();
        saved.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        saved.setCode("ABC123");
        saved.setDescription(couponDTO.getDescription());
        saved.setDiscountValue(couponDTO.getDiscountValue());
        saved.setExpirationDate(couponDTO.getExpirationDate());
        saved.setStatus(CouponStatusEnum.ACTIVE);
        saved.setPublished(true);
        saved.setRedeemed(false);

        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);


        CouponResponse response = couponService.create(couponDTO);


        assertThat(response.getPublished()).isTrue();


        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isTrue();
    }


    @Test
    void shouldTruncateCodeToSixCharactersWhenSanitizedIsLongerThanSix() {

        couponDTO.setCode("ABC-123-XYZ");

        CouponEntity saved = new CouponEntity();
        saved.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        saved.setCode("ABC123");
        saved.setDescription(couponDTO.getDescription());
        saved.setDiscountValue(couponDTO.getDiscountValue());
        saved.setExpirationDate(couponDTO.getExpirationDate());
        saved.setStatus(CouponStatusEnum.ACTIVE);
        saved.setPublished(false);
        saved.setRedeemed(false);

        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);


        CouponResponse response = couponService.create(couponDTO);


        assertThat(response.getCode()).isEqualTo("ABC123");


        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("ABC123");
    }


    @Test
    void shouldDefaultPublishedToFalseWhenPublishedIsNull() {

        couponDTO.setPublished(null);

        CouponEntity saved = new CouponEntity();
        saved.setId(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));
        saved.setCode("ABC123");
        saved.setDescription(couponDTO.getDescription());
        saved.setDiscountValue(couponDTO.getDiscountValue());
        saved.setExpirationDate(couponDTO.getExpirationDate());
        saved.setStatus(CouponStatusEnum.ACTIVE);
        saved.setPublished(false);
        saved.setRedeemed(false);

        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);


        CouponResponse response = couponService.create(couponDTO);


        assertThat(response.getPublished()).isFalse();

        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isFalse();
    }


    @Test
    void shouldFailWhenDiscountValueIsLessThanMin() {
        couponDTO.setDiscountValue(0.4);

        assertThatThrownBy(() -> couponService.create(couponDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("discount");

        verifyNoInteractions(couponRepository);
    }

    @Test
    void shouldFailWhenExpirationDateIsInThePast() {
        couponDTO.setExpirationDate(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> couponService.create(couponDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expiration");

        verifyNoInteractions(couponRepository);
    }

    @Test
    void shouldFailWhenCodeAfterSanitizeHasLessThanSixCharacters() {
        couponDTO.setCode("A!1");

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
        coupon.setStatus(CouponStatusEnum.ACTIVE);

        when(couponRepository.findById(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"))).thenReturn(Optional.of(coupon));


        couponService.delete(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"));


        assertThat(coupon.getStatus()).isEqualTo(CouponStatusEnum.DELETED);
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
        coupon.setStatus(CouponStatusEnum.DELETED);

        when(couponRepository.findById(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50"))).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.delete(UUID.fromString("d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50")))
                .isInstanceOf(CouponAlreadyDeletedException.class);

        verify(couponRepository, never()).save(any());
    }

}
