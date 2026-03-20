package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public ChatMessage save(ChatMessage message) {
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<ChatMessage> getLast50Messages() {
        return messageRepository.findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType.CHAT);
    }

    @Transactional
    public void deleteAllMessages() {
        messageRepository.deleteAllMessages();
    }
}
