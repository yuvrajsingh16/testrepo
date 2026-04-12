package com.demo.oncall.service;

import com.demo.oncall.domain.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class ShippingLabelFormatter {

    public Optional<String> format(UserProfile user) {
        if (user.getAddress() == null || user.getAddress().isBlank()) {
            return Optional.empty();
        }

        String normalizedAddress = user.getAddress().toUpperCase(Locale.ROOT);
        return Optional.of(user.getName() + "\n" + normalizedAddress);
    }
}
