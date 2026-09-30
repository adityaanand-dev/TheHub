package com.thehub.controller;

import com.thehub.dto.ChatMessageRequest;
import com.thehub.dto.ChatMessageResponse;
import com.thehub.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Project Chat", description = "Project-linked direct messaging between Client and Creator with Kafka event notification")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/projects/{projectId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a project-linked message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ChatMessageResponse response = chatService.sendMessage(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get conversation history for a project")
    public ResponseEntity<List<ChatMessageResponse>> getProjectMessages(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(chatService.getProjectMessages(projectId, userDetails.getUsername()));
    }
}
