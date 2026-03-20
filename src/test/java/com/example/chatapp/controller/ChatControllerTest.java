package com.example.chatapp.controller;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.websocket.UserSessionRegistry;
import com.example.chatapp.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private UserSessionRegistry userSessionRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    // ── sendMessage ───────────────────────────────────────────────────────────

    @Test
    void sendMessage_delegatesToServiceAndReturnsResult() {
        ChatMessage incoming = new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT);
        ChatMessage saved = new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT);
        when(messageService.save(incoming)).thenReturn(saved);

        ChatMessage result = chatController.sendMessage(incoming);

        assertThat(result).isSameAs(saved);
        verify(messageService).save(incoming);
    }

    // ── addUser ───────────────────────────────────────────────────────────────

    @Test
    void addUser_storesUsernameInSessionAttributes() {
        ChatMessage message = new ChatMessage("Пешо", null, ChatMessage.MessageType.JOIN);
        SimpMessageHeaderAccessor accessor = mockHeaderAccessor("session-1");

        chatController.addUser(message, accessor);

        assertThat(accessor.getSessionAttributes()).containsEntry("username", "Пешо");
    }

    @Test
    void addUser_registersSessionInRegistry() {
        ChatMessage message = new ChatMessage("Пешо", null, ChatMessage.MessageType.JOIN);
        SimpMessageHeaderAccessor accessor = mockHeaderAccessor("session-1");

        chatController.addUser(message, accessor);

        verify(userSessionRegistry).add("session-1", "Пешо");
    }

    @Test
    void addUser_broadcastsUpdatedUsersListToTopic() {
        ChatMessage message = new ChatMessage("Пешо", null, ChatMessage.MessageType.JOIN);
        SimpMessageHeaderAccessor accessor = mockHeaderAccessor("session-1");
        List<String> onlineUsers = List.of("Пешо");
        when(userSessionRegistry.getOnlineUsernames()).thenReturn(onlineUsers);

        chatController.addUser(message, accessor);

        verify(messagingTemplate).convertAndSend("/topic/users", onlineUsers);
    }

    @Test
    void addUser_returnsOriginalMessage() {
        ChatMessage message = new ChatMessage("Пешо", null, ChatMessage.MessageType.JOIN);
        SimpMessageHeaderAccessor accessor = mockHeaderAccessor("session-1");

        ChatMessage result = chatController.addUser(message, accessor);

        assertThat(result).isSameAs(message);
    }

    // ── typing ────────────────────────────────────────────────────────────────

    @Test
    void typing_returnsMessageUnchanged() {
        ChatMessage message = new ChatMessage("Пешо", null, ChatMessage.MessageType.TYPING);

        ChatMessage result = chatController.typing(message);

        assertThat(result).isSameAs(message);
        verifyNoInteractions(messageService, userSessionRegistry, messagingTemplate);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SimpMessageHeaderAccessor mockHeaderAccessor(String sessionId) {
        SimpMessageHeaderAccessor accessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttrs = new HashMap<>();
        when(accessor.getSessionId()).thenReturn(sessionId);
        when(accessor.getSessionAttributes()).thenReturn(sessionAttrs);
        return accessor;
    }
}
