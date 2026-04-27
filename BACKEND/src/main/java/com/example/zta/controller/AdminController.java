package com.example.zta.controller;

import com.example.zta.user.User;
import com.example.zta.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Map<String, Object> getUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("enabled", user.isEnabled());
            userMap.put("failedLogins", user.getFailedLogins());
            userMap.put("lastLoginAt", user.getLastLoginAt());
            userMap.put("lastIp", user.getLastIp());
            userMap.put("roles", user.getRoles().stream()
                .map(r -> r.getName().replace("ROLE_", ""))
                .collect(Collectors.toList()));
            return userMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("total", users.size());
        return response;
    }
}


