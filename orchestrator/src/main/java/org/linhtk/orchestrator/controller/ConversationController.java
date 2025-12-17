package org.linhtk.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.dto.ChatMessageResponseDto;
import org.linhtk.orchestrator.dto.ConversationCreateRequestDto;
import org.linhtk.orchestrator.dto.ConversationResponseDto;
import org.linhtk.orchestrator.dto.ConversationUpdateRequestDto;
import org.linhtk.orchestrator.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for managing conversations.
 * Provides endpoints for conversation operations including creation, retrieval, and message management.
 * <p>
 * Endpoints:
 * - GET /api/conversations - List conversations by agent and user
 * - POST /api/conversations - Create new conversation
 * - GET /api/conversations/{conversationId} - Get conversation by ID
 * - PUT /api/conversations/{conversationId} - Update conversation
 * - GET /api/conversations/{conversationId}/messages - Get messages in a conversation
 */
@RestController
@RequestMapping("/api/conversations")
@Slf4j
@Tag(name = "Conversation Management", description = "APIs for managing conversations and chat messages")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Lists all conversations for a specific agent and user.
     * Returns conversations ordered by creation date (most recent first).
     *
     * @param agentId The unique identifier of the agent
     * @param userId  The unique identifier of the user
     * @return ResponseEntity containing list of conversation summaries
     */
    @GetMapping
    @Operation(summary = "List conversations", description = "Retrieves all conversations for a specific agent and user")
    public ResponseEntity<List<ConversationResponseDto>> listConversations(
            @RequestParam String agentId,
            @RequestParam String userId) {
        log.info("REST request to list conversations for agent: {} and user: {}", agentId, userId);

        try {
            List<ConversationResponseDto> conversations =
                    conversationService.listConversationsByAgentAndUser(agentId, userId);

            log.debug("Successfully retrieved {} conversations", conversations.size());
            return ResponseEntity.ok(conversations);

        } catch (Exception e) {
            log.error("Error listing conversations for agent: {} and user: {}", agentId, userId, e);
            throw e;
        }
    }

    /**
     * Retrieves all messages within a specific conversation.
     * Returns messages ordered chronologically (oldest first).
     *
     * @param conversationId The unique identifier of the conversation
     * @return ResponseEntity containing list of chat messages
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get conversation messages", description = "Retrieves all messages in a specific conversation")
    public ResponseEntity<List<ChatMessageResponseDto>> listMessages(
            @PathVariable String conversationId) {
        log.info("REST request to list messages for conversation: {}", conversationId);

        try {
            List<ChatMessageResponseDto> messages =
                    conversationService.listMessagesByConversation(conversationId);

            log.debug("Successfully retrieved {} messages for conversation: {}",
                    messages.size(), conversationId);
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            log.error("Error listing messages for conversation: {}", conversationId, e);
            throw e;
        }
    }

    /**
     * Creates a new conversation without messages.
     * Useful for pre-creating conversations before starting a chat session.
     *
     * @param requestDto The conversation creation request containing agent ID and optional name
     * @return ResponseEntity containing created conversation with HTTP 201 status
     */
    @PostMapping
    @Operation(summary = "Create conversation", description = "Creates a new conversation for an agent")
    public ResponseEntity<ConversationResponseDto> createConversation(
            @Valid @RequestBody ConversationCreateRequestDto requestDto) {
        log.info("REST request to create conversation for agent: {}", requestDto.getAgentId());

        try {
            ConversationResponseDto conversation =
                    conversationService.createSimpleConversation(requestDto.getAgentId(), requestDto.getName());

            log.debug("Successfully created conversation with ID: {}", conversation.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);

        } catch (Exception e) {
            log.error("Error creating conversation for agent: {}", requestDto.getAgentId(), e);
            throw e;
        }
    }

    /**
     * Retrieves a specific conversation by ID.
     * Returns conversation details without messages.
     *
     * @param conversationId The unique identifier of the conversation
     * @return ResponseEntity containing conversation details
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation by ID", description = "Retrieves a specific conversation by its ID")
    public ResponseEntity<ConversationResponseDto> getConversation(
            @PathVariable String conversationId) {
        log.info("REST request to get conversation by ID: {}", conversationId);

        try {
            ConversationResponseDto conversation =
                    conversationService.getConversationById(conversationId);

            log.debug("Successfully retrieved conversation: {}", conversationId);
            return ResponseEntity.ok(conversation);

        } catch (Exception e) {
            log.error("Error getting conversation by ID: {}", conversationId, e);
            throw e;
        }
    }

    /**
     * Updates an existing conversation.
     * Currently supports updating the conversation name.
     *
     * @param conversationId The unique identifier of the conversation to update
     * @param requestDto     The conversation update request with new values
     * @return ResponseEntity containing updated conversation details
     */
    @PutMapping("/{conversationId}")
    @Operation(summary = "Update conversation", description = "Updates an existing conversation (e.g., rename)")
    public ResponseEntity<ConversationResponseDto> updateConversation(
            @PathVariable String conversationId,
            @Valid @RequestBody ConversationUpdateRequestDto requestDto) {
        log.info("REST request to update conversation: {}", conversationId);

        try {
            conversationService.updateConversationName(conversationId, requestDto.getName());

            // Retrieve updated conversation to return to client
            ConversationResponseDto updatedConversation =
                    conversationService.getConversationById(conversationId);

            log.debug("Successfully updated conversation: {}", conversationId);
            return ResponseEntity.ok(updatedConversation);

        } catch (Exception e) {
            log.error("Error updating conversation: {}", conversationId, e);
            throw e;
        }
    }
}
