package com.example.chatapp.controller;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.ReadReceipt;
import com.example.chatapp.websocket.UserSessionRegistry;
import com.example.chatapp.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {

    private final MessageService messageService;
    private final UserSessionRegistry userSessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MessageService messageService,
                          UserSessionRegistry userSessionRegistry,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userSessionRegistry = userSessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        return messageService.save(message);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String username = message.getSender();

        headerAccessor.getSessionAttributes().put("username", username);
        userSessionRegistry.add(sessionId, username);

        messagingTemplate.convertAndSend("/topic/users", userSessionRegistry.getOnlineUsernames());

        return message;
    }

    @MessageMapping("/chat.typing")
    @SendTo("/topic/typing")
    public ChatMessage typing(@Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/chat.read")
    @SendTo("/topic/read")
    public Map<String, Long> markRead(@Payload ReadReceipt receipt) {
        userSessionRegistry.updateLastRead(receipt.getUsername(), receipt.getMessageId());
        return userSessionRegistry.getUserLastRead();
    }
}
