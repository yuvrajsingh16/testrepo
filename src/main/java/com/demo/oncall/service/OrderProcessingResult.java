package com.demo.oncall.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public final class OrderProcessingResult {

    private final Status status;
    private final String orderId;
    private final String customer;
    private final String product;
    private final int quantity;
    private final String shippingAddress;
    private final String error;
    private final String detail;
    private final LocalDateTime timestamp;

    private OrderProcessingResult(
            Status status,
            String orderId,
            String customer,
            String product,
            int quantity,
            String shippingAddress,
            String error,
            String detail,
            LocalDateTime timestamp) {
        this.status = Objects.requireNonNull(status, "status");
        this.orderId = orderId;
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.shippingAddress = shippingAddress;
        this.error = error;
        this.detail = detail;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    }

    public static OrderProcessingResult success(
            String orderId,
            String customer,
            String product,
            int quantity,
            String shippingAddress,
            LocalDateTime timestamp) {
        return new OrderProcessingResult(
                Status.SUCCESS,
                Objects.requireNonNull(orderId, "orderId"),
                Objects.requireNonNull(customer, "customer"),
                Objects.requireNonNull(product, "product"),
                quantity,
                Objects.requireNonNull(shippingAddress, "shippingAddress"),
                null,
                null,
                timestamp);
    }

    public static OrderProcessingResult error(String error, String detail, LocalDateTime timestamp) {
        return new OrderProcessingResult(Status.ERROR, null, null, null, 0, null,
                Objects.requireNonNull(error, "error"), detail, timestamp);
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getOrderId() {
        return Optional.ofNullable(orderId);
    }

    public Optional<String> getCustomer() {
        return Optional.ofNullable(customer);
    }

    public Optional<String> getProduct() {
        return Optional.ofNullable(product);
    }

    public int getQuantity() {
        return quantity;
    }

    public Optional<String> getShippingAddress() {
        return Optional.ofNullable(shippingAddress);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public enum Status {
        SUCCESS,
        ERROR
    }
}
