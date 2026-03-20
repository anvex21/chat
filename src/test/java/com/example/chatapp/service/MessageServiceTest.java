package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void save_setsSentAtBeforePersisting() {
        ChatMessage message = new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        messageService.save(message);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    void save_returnsPersistedMessage() {
        ChatMessage message = new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT);
        ChatMessage saved = new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT);
        when(messageRepository.save(any())).thenReturn(saved);

        ChatMessage result = messageService.save(message);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void getLast50Messages_delegatesToRepository() {
        List<ChatMessage> expected = List.of(
                new ChatMessage("Пешо", "Здравей", ChatMessage.MessageType.CHAT),
                new ChatMessage("Мария", "Привет", ChatMessage.MessageType.CHAT)
        );
        when(messageRepository.findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.CHAT))
                .thenReturn(expected);

        List<ChatMessage> result = messageService.getLast50Messages();

        assertThat(result).isSameAs(expected);
        verify(messageRepository).findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.CHAT);
    }

    @Test
    void getLast50Messages_queriesOnlyChatType() {
        messageService.getLast50Messages();

        verify(messageRepository).findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.CHAT);
        verify(messageRepository, never()).findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.JOIN);
        verify(messageRepository, never()).findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.LEAVE);
    }
}
