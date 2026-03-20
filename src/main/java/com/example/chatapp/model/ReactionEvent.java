package com.example.chatapp.model;

public class ReactionEvent {

    private Long messageId;
    private String emoji;
    private String username;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
