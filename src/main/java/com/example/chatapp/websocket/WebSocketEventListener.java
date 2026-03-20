package com.example.chatapp.websocket;

import com.example.chatapp.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserSessionRegistry userSessionRegistry;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                   UserSessionRegistry userSessionRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userSessionRegistry = userSessionRegistry;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            logger.info("User disconnected: {}", username);

            userSessionRegistry.remove(sessionId);
            userSessionRegistry.clearLastRead(username);
            messagingTemplate.convertAndSend("/topic/users", userSessionRegistry.getOnlineUsernames());
            messagingTemplate.convertAndSend("/topic/read", userSessionRegistry.getUserLastRead());

            ChatMessage leaveMessage = new ChatMessage(username, username + " напусна чата", ChatMessage.MessageType.LEAVE);
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}
