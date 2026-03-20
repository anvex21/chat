package com.example.chatapp.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserSessionRegistryTest {

    private UserSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new UserSessionRegistry();
    }

    @Test
    void startsEmpty() {
        assertThat(registry.getOnlineUsernames()).isEmpty();
    }

    @Test
    void addUser_appearsInList() {
        registry.add("session-1", "Пешо");

        assertThat(registry.getOnlineUsernames()).containsExactly("Пешо");
    }

    @Test
    void removeUser_disappearsFromList() {
        registry.add("session-1", "Пешо");
        registry.remove("session-1");

        assertThat(registry.getOnlineUsernames()).isEmpty();
    }

    @Test
    void getOnlineUsernames_returnsSortedAlphabetically() {
        registry.add("session-1", "Мария");
        registry.add("session-2", "Антон");
        registry.add("session-3", "Пешо");

        assertThat(registry.getOnlineUsernames()).containsExactly("Антон", "Мария", "Пешо");
    }

    @Test
    void removeNonExistentSession_doesNothing() {
        registry.add("session-1", "Пешо");

        registry.remove("session-999");

        assertThat(registry.getOnlineUsernames()).containsExactly("Пешо");
    }

    @Test
    void twoSessionsSameUsername_bothAppear() {
        registry.add("session-1", "Пешо");
        registry.add("session-2", "Пешо");

        assertThat(registry.getOnlineUsernames()).hasSize(2);
    }

    @Test
    void removeOneSession_otherSessionRemains() {
        registry.add("session-1", "Пешо");
        registry.add("session-2", "Пешо");

        registry.remove("session-1");

        assertThat(registry.getOnlineUsernames()).hasSize(1);
    }
}
