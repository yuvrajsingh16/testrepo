package com.demo.oncall.repository;

import com.demo.oncall.domain.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserProfile> usersById;

    public InMemoryUserRepository() {
        Map<String, UserProfile> seeded = new LinkedHashMap<>();
        seeded.put("1001", new UserProfile("1001", "Alice Johnson", "alice@example.com", "123 Main St, Springfield, IL 62704"));
        seeded.put("1002", new UserProfile("1002", "Bob Martinez", "bob@example.com", null));
        seeded.put("1003", new UserProfile("1003", "Charlie Davis", "charlie@example.com", "789 Oak Ave, Portland, OR 97201"));
        this.usersById = Map.copyOf(seeded);
    }

    @Override
    public Optional<UserProfile> findById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public List<UserProfile> findAll() {
        return List.copyOf(usersById.values());
    }
}
