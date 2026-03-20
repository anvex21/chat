package com.example.chatapp.model;

import java.util.List;
import java.util.Map;

public class ReactionUpdate {

    private Long messageId;
    private Map<String, List<String>> reactions;

    public ReactionUpdate(Long messageId, Map<String, List<String>> reactions) {
        this.messageId = messageId;
        this.reactions = reactions;
    }

    public Long getMessageId() { return messageId; }
    public Map<String, List<String>> getReactions() { return reactions; }
}
