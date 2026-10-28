package com.voidapp.application.service;

import com.voidapp.domain.model.User;
import com.voidapp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(@NonNull UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveUser(@NonNull User user) {
        return userRepository.save(user);
    }
}
