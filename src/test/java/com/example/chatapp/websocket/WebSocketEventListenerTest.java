package com.example.chatapp.websocket;

import com.example.chatapp.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private UserSessionRegistry userSessionRegistry;

    @InjectMocks
    private WebSocketEventListener listener;

    @Test
    void disconnect_withUsername_removesSessionFromRegistry() {
        SessionDisconnectEvent event = buildDisconnectEvent("session-1", "Пешо");

        listener.handleWebSocketDisconnectListener(event);

        verify(userSessionRegistry).remove("session-1");
    }

    @Test
    void disconnect_withUsername_broadcastsUpdatedUsersList() {
        SessionDisconnectEvent event = buildDisconnectEvent("session-1", "Пешо");
        List<String> remaining = List.of("Мария");
        when(userSessionRegistry.getOnlineUsernames()).thenReturn(remaining);

        listener.handleWebSocketDisconnectListener(event);

        verify(messagingTemplate).convertAndSend("/topic/users", remaining);
    }

    @Test
    void disconnect_withUsername_sendsLeaveMessageToPublicTopic() {
        SessionDisconnectEvent event = buildDisconnectEvent("session-1", "Пешо");

        listener.handleWebSocketDisconnectListener(event);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), captor.capture());

        ChatMessage leaveMsg = captor.getValue();
        assertThat(leaveMsg.getSender()).isEqualTo("Пешо");
        assertThat(leaveMsg.getType()).isEqualTo(ChatMessage.MessageType.LEAVE);
    }

    @Test
    void disconnect_withoutUsername_doesNotBroadcastOrRemove() {
        SessionDisconnectEvent event = buildDisconnectEvent("session-1", null);

        listener.handleWebSocketDisconnectListener(event);

        verifyNoInteractions(userSessionRegistry, messagingTemplate);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SessionDisconnectEvent buildDisconnectEvent(String sessionId, String username) {
        Map<String, Object> sessionAttrs = new HashMap<>();
        if (username != null) {
            sessionAttrs.put("username", username);
        }

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionId(sessionId);
        accessor.setSessionAttributes(sessionAttrs);

        Message<byte[]> message = MessageBuilder
                .createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getMessage()).thenReturn(message);
        return event;
    }
}
