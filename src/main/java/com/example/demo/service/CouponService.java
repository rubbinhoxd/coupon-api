package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponResponse;
import com.example.demo.entities.CouponEntity;
import com.example.demo.enums.CouponStatusEnum;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.CodeNotFoundException;
import com.example.demo.exceptions.CouponAlreadyDeletedException;
import com.example.demo.exceptions.CouponNotFoundException;
import com.example.demo.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository repository;

    public CouponService(CouponRepository repository) {
        this.repository = repository;
    }

    public CouponResponse create(CouponDTO request) {

        String sanitizedCode = sanitizeCode(request.getCode());

        if (request.getDiscountValue() == null || request.getDiscountValue() < 0.5) {
            throw new BusinessException("discountValue must be at least 0.5");
        }

        if (request.getExpirationDate() == null ||
                request.getExpirationDate().isBefore(Instant.now())) {
            throw new BusinessException("expirationDate cannot be in the past");
        }

        CouponEntity entity = new CouponEntity();
        entity.setCode(sanitizedCode);
        entity.setDescription(request.getDescription());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setExpirationDate(request.getExpirationDate());
        entity.setPublished(Boolean.TRUE.equals(request.getPublished()));
        entity.setStatus(CouponStatusEnum.ACTIVE);
        entity.setRedeemed(false);

        entity = repository.save(entity);

        return toResponse(entity);
    }

    public CouponResponse findById(UUID id) {
        CouponEntity entity = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));


        if (CouponStatusEnum.DELETED.equals(entity.getStatus())) {
            throw new CouponNotFoundException("Coupon not found");
        }

        return toResponse(entity);
    }

    public void delete(UUID id) {
        CouponEntity entity = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));

        if (CouponStatusEnum.DELETED.equals(entity.getStatus())) {
            throw new CouponAlreadyDeletedException("Coupon already deleted");
        }

        entity.setStatus(CouponStatusEnum.DELETED);
        repository.save(entity);
    }



    private String sanitizeCode(String rawCode) {
        if (rawCode == null) {
            throw new CodeNotFoundException("code is required");
        }


        String sanitized = rawCode.replaceAll("[^A-Za-z0-9]", "");


        if (sanitized.length() > 6) {
            sanitized = sanitized.substring(0, 6);
        }

        if (sanitized.length() < 6) {
            throw new BusinessException("code must have 6 alphanumeric characters after sanitization");
        }

        return sanitized;
    }

    private CouponResponse toResponse(CouponEntity entity) {
        CouponResponse resp = new CouponResponse();
        resp.setId(entity.getId());
        resp.setCode(entity.getCode());
        resp.setDescription(entity.getDescription());
        resp.setDiscountValue(entity.getDiscountValue());
        resp.setExpirationDate(entity.getExpirationDate());
        resp.setStatus(entity.getStatus());
        resp.setPublished(entity.getPublished());
        resp.setRedeemed(entity.getRedeemed());

        return resp;
    }
}
