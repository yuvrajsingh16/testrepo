package com.demo.oncall.controller;

import com.demo.oncall.service.OrderProcessingResult;
import com.demo.oncall.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/process-order")
    public Map<String, Object> processOrder(
            @RequestParam String userId,
            @RequestParam String product,
            @RequestParam int quantity) {
        OrderProcessingResult result = orderService.processOrder(userId, product, quantity);

        if (result.getStatus() == OrderProcessingResult.Status.SUCCESS) {
            return Map.of(
                    "status", "success",
                    "orderId", result.getOrderId().orElseThrow(),
                    "customer", result.getCustomer().orElseThrow(),
                    "product", result.getProduct().orElseThrow(),
                    "quantity", result.getQuantity(),
                    "shippingAddress", result.getShippingAddress().orElseThrow(),
                    "timestamp", result.getTimestamp().toString()
            );
        }

        return Map.of(
                "status", "error",
                "error", result.getError().orElse("Unknown error"),
                "detail", result.getDetail().orElse(""),
                "timestamp", result.getTimestamp().toString()
        );
    }
}
