package com.example.chatapp.repository;

import com.example.chatapp.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop50ByTypeOrderBySentAtAsc(ChatMessage.MessageType type);

    @Modifying
    @Query("DELETE FROM ChatMessage m")
    void deleteAllMessages();
}
