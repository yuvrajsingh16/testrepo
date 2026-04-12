package com.demo.oncall.service;

import com.demo.oncall.domain.UserProfile;
import com.demo.oncall.repository.UserRepository;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final UserRepository userRepository;
    private final ShippingLabelFormatter shippingLabelFormatter;

    public OrderService(UserRepository userRepository, ShippingLabelFormatter shippingLabelFormatter) {
        this.userRepository = userRepository;
        this.shippingLabelFormatter = shippingLabelFormatter;
    }

    public OrderProcessingResult processOrder(String userId, String product, int quantity) {
        log.info("Received order request: userId={}, product={}, quantity={}", userId, product, quantity);

        try {
            Optional<UserProfile> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                log.warn("User not found: {}", userId);
                return OrderProcessingResult.error("User not found: " + userId, null, LocalDateTime.now());
            }

            UserProfile userProfile = user.get();
            log.info("Processing order for user: {} ({})", userProfile.getName(), userProfile.getEmail());

            Optional<String> shippingLabel = shippingLabelFormatter.format(userProfile);
            if (shippingLabel.isEmpty()) {
                log.warn("Cannot process order: shipping address missing for userId={}", userId);
                NewRelic.addCustomParameter("userId", userId);
                NewRelic.addCustomParameter("product", product);
                NewRelic.addCustomParameter("errorType", "MissingShippingAddress");

                return OrderProcessingResult.error(
                        "Missing shipping address for user " + userId,
                        "User profile is missing a shipping address. Please update the address and retry.",
                        LocalDateTime.now());
            }

            String orderId = "ORD-" + System.currentTimeMillis();
            log.info("Order {} created successfully. Shipping to: {}", orderId, shippingLabel.get());

            NewRelic.addCustomParameter("orderId", orderId);
            NewRelic.addCustomParameter("userId", userId);
            NewRelic.addCustomParameter("product", product);

            return OrderProcessingResult.success(
                    orderId,
                    userProfile.getName(),
                    product,
                    quantity,
                    shippingLabel.get(),
                    LocalDateTime.now());

        } catch (RuntimeException e) {
            log.error("Unexpected error while processing order for userId={}, product={}, quantity={}", userId, product, quantity, e);

            NewRelic.noticeError(e, Map.of(
                    "userId", userId,
                    "product", product,
                    "component", "OrderProcessing"
            ));

            return OrderProcessingResult.error(
                    "Unexpected error while processing order",
                    null,
                    LocalDateTime.now());
        }
    }
}
