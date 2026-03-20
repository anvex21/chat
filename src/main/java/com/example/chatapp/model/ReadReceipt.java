package com.example.chatapp.model;

public class ReadReceipt {

    private String username;
    private Long messageId;

    public ReadReceipt() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
}
