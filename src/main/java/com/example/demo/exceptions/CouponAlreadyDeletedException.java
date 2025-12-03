package com.example.demo.exceptions;

public class CouponAlreadyDeletedException extends RuntimeException {
    public CouponAlreadyDeletedException(String message) {
        super(message);
    }
}
