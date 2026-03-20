package com.example.chatapp.controller;

import com.example.chatapp.websocket.UserSessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserSessionRegistry userSessionRegistry;

    public UserRestController(UserSessionRegistry userSessionRegistry) {
        this.userSessionRegistry = userSessionRegistry;
    }

    @GetMapping("/check")
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        return Map.of("taken", userSessionRegistry.isUsernameTaken(username));
    }
}
