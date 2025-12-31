package com.example.demo.dto;

import com.example.demo.enums.CouponStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class CouponResponse {

    private UUID id;
    private String code;
    private String description;
    private Double discountValue;
    private Instant expirationDate;
    private CouponStatusEnum status;
    private Boolean published;
    private Boolean redeemed;

    public CouponResponse() {
    }

    public CouponResponse(UUID id, String code, String description, Double discountValue, Instant expirationDate, CouponStatusEnum status, Boolean published, Boolean redeemed) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.redeemed = redeemed;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CouponStatusEnum getStatus() {
        return status;
    }

    public void setStatus(CouponStatusEnum status) {
        this.status = status;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Boolean getRedeemed() {
        return redeemed;
    }

    public void setRedeemed(Boolean redeemed) {
        this.redeemed = redeemed;
    }
}
