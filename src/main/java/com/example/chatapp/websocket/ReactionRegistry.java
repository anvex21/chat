package com.example.chatapp.websocket;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReactionRegistry {

    // messageId -> emoji -> set of usernames
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Set<String>>> reactions =
            new ConcurrentHashMap<>();

    /**
     * Toggles a reaction: adds if not present, removes if already reacted.
     * Returns the updated reactions map for that message.
     */
    public Map<String, List<String>> toggle(Long messageId, String emoji, String username) {
        ConcurrentHashMap<String, Set<String>> msgReactions =
                reactions.computeIfAbsent(messageId, k -> new ConcurrentHashMap<>());

        Set<String> users = msgReactions.computeIfAbsent(emoji, k -> ConcurrentHashMap.newKeySet());
        if (!users.remove(username)) {
            users.add(username);
        }
        if (users.isEmpty()) {
            msgReactions.remove(emoji);
        }

        return snapshot(messageId);
    }

    public Map<String, List<String>> snapshot(Long messageId) {
        ConcurrentHashMap<String, Set<String>> msgReactions = reactions.get(messageId);
        if (msgReactions == null) return Collections.emptyMap();

        Map<String, List<String>> result = new HashMap<>();
        msgReactions.forEach((emoji, users) -> {
            if (!users.isEmpty()) result.put(emoji, new ArrayList<>(users));
        });
        return result;
    }
}
