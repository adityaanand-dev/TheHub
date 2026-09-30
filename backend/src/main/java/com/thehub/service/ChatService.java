package com.thehub.service;

import com.thehub.dto.ChatMessageRequest;
import com.thehub.dto.ChatMessageResponse;
import com.thehub.dto.event.NotificationEvent;
import com.thehub.entity.ChatMessage;
import com.thehub.entity.Project;
import com.thehub.entity.Role;
import com.thehub.entity.User;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.kafka.KafkaEventProducer;
import com.thehub.repository.ChatMessageRepository;
import com.thehub.repository.ProjectApplicationRepository;
import com.thehub.repository.ProjectRepository;
import com.thehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       ProjectRepository projectRepository,
                       ProjectApplicationRepository applicationRepository,
                       UserRepository userRepository,
                       KafkaEventProducer kafkaEventProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long projectId, ChatMessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + senderEmail));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // Determine recipient
        User recipient;
        if (request.getRecipientId() != null) {
            recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + request.getRecipientId()));
        } else {
            // Infer recipient from project context
            if (sender.getId().equals(project.getClient().getId())) {
                if (project.getSelectedCreator() != null) {
                    recipient = project.getSelectedCreator();
                } else {
                    throw new BadRequestException("Please specify recipient for this project inquiry.");
                }
            } else {
                recipient = project.getClient();
            }
        }

        // Verify participants
        boolean isClient = sender.getId().equals(project.getClient().getId()) || recipient.getId().equals(project.getClient().getId());
        boolean isCreatorOrApplicant = (project.getSelectedCreator() != null &&
                (sender.getId().equals(project.getSelectedCreator().getId()) || recipient.getId().equals(project.getSelectedCreator().getId())))
                || applicationRepository.existsByProjectIdAndCreatorId(projectId, sender.getId())
                || applicationRepository.existsByProjectIdAndCreatorId(projectId, recipient.getId());

        if (!isClient && !isCreatorOrApplicant && sender.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("You are not an authorized participant for this project conversation.");
        }

        ChatMessage message = new ChatMessage(project, sender, recipient, request.getContent().trim());
        ChatMessage saved = chatMessageRepository.save(message);

        // Kafka Event to Recipient: New Message
        NotificationEvent notif = new NotificationEvent(
                recipient.getId(),
                recipient.getEmail(),
                "New Message from " + sender.getFullName(),
                sender.getFullName() + " sent you a message regarding project '" + project.getTitle() + "'.",
                "MESSAGE_SENT"
        );
        kafkaEventProducer.publishNotificationEvent(notif);

        return mapToResponse(saved);
    }

    @Transactional
    public List<ChatMessageResponse> getProjectMessages(Long projectId, String username) {
        User user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // Verify participant
        boolean isClient = user.getId().equals(project.getClient().getId());
        boolean isSelectedCreator = project.getSelectedCreator() != null && user.getId().equals(project.getSelectedCreator().getId());
        boolean hasApplied = applicationRepository.existsByProjectIdAndCreatorId(projectId, user.getId());

        if (!isClient && !isSelectedCreator && !hasApplied && user.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Not authorized to view messages for this project.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        // Mark incoming messages as read
        for (ChatMessage msg : messages) {
            if (msg.getRecipient().getId().equals(user.getId()) && !msg.isRead()) {
                msg.setRead(true);
            }
        }

        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage msg) {
        ChatMessageResponse resp = new ChatMessageResponse();
        resp.setId(msg.getId());
        resp.setProjectId(msg.getProject().getId());
        resp.setSenderId(msg.getSender().getId());
        resp.setSenderName(msg.getSender().getFullName());
        resp.setSenderRole(msg.getSender().getRole().name());
        resp.setRecipientId(msg.getRecipient().getId());
        resp.setRecipientName(msg.getRecipient().getFullName());
        resp.setContent(msg.getContent());
        resp.setRead(msg.isRead());
        resp.setCreatedAt(msg.getCreatedAt());
        return resp;
    }
}
