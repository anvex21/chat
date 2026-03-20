package com.example.chatapp.websocket;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionRegistry {

    // sessionId → username
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

    // username → last read messageId
    private final ConcurrentHashMap<String, Long> userLastRead = new ConcurrentHashMap<>();

    public void add(String sessionId, String username) {
        sessions.put(sessionId, username);
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public List<String> getOnlineUsernames() {
        List<String> names = new ArrayList<>(sessions.values());
        Collections.sort(names);
        return names;
    }

    public void updateLastRead(String username, Long messageId) {
        userLastRead.merge(username, messageId, Math::max);
    }

    public void clearLastRead(String username) {
        userLastRead.remove(username);
    }

    public Map<String, Long> getUserLastRead() {
        return new HashMap<>(userLastRead);
    }
}
