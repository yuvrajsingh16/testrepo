package com.demo.oncall.domain;

import java.util.Objects;

public final class UserProfile {

    private final String id;
    private final String name;
    private final String email;
    private final String address;

    public UserProfile(String id, String name, String email, String address) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
