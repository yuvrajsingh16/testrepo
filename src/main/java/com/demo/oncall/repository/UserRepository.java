package com.demo.oncall.repository;

import com.demo.oncall.domain.UserProfile;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<UserProfile> findById(String userId);

    List<UserProfile> findAll();
}
