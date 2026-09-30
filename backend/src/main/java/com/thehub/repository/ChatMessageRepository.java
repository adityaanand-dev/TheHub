package com.thehub.repository;

import com.thehub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    long countByProjectIdAndRecipientIdAndIsReadFalse(Long projectId, Long recipientId);
}
