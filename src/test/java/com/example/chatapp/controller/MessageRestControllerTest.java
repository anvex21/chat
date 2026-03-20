package com.example.chatapp.controller;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRestControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageRestController controller;

    @Test
    void getMessageHistory_returnsMessagesFromService() {
        List<ChatMessage> expected = List.of(
                new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT),
                new ChatMessage("Мария", "Привет", ChatMessage.MessageType.CHAT)
        );
        when(messageService.getLast50Messages()).thenReturn(expected);

        List<ChatMessage> result = controller.getMessageHistory();

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getMessageHistory_returnsEmptyList_whenNoMessages() {
        when(messageService.getLast50Messages()).thenReturn(List.of());

        List<ChatMessage> result = controller.getMessageHistory();

        assertThat(result).isEmpty();
    }

    @Test
    void getMessageHistory_delegatesToService() {
        when(messageService.getLast50Messages()).thenReturn(List.of());

        controller.getMessageHistory();

        verify(messageService).getLast50Messages();
    }
}
