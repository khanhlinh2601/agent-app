package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.dto.ChatMessageResponseDto;
import org.linhtk.orchestrator.dto.ChatRequestDto;
import org.linhtk.orchestrator.dto.ChatResponseDto;
import org.linhtk.orchestrator.dto.ConversationResponseDto;
import org.linhtk.orchestrator.mapper.ChatMessageMapper;
import org.linhtk.orchestrator.mapper.ConversationMapper;
import org.linhtk.orchestrator.model.conversation.ChatMessage;
import org.linhtk.orchestrator.model.conversation.Conversation;
import org.linhtk.orchestrator.repository.ChatMessageRepository;
import org.linhtk.orchestrator.repository.ConversationRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversationService {
    private static final int MESSAGE_TYPE_USER = 0;
    private static final int MESSAGE_TYPE_ASSISTANT = 1;
    private static final int MESSAGE_TYPE_SYSTEM = 2;
    private static final int CONVERSATION_NAME_MAX_LENGTH = 100;
    
    private final ChatModelService chatModelService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ConversationService(ChatModelService chatModelService,
                               ConversationRepository conversationRepository,
                               ChatMessageRepository chatMessageRepository,
                               ConversationMapper conversationMapper,
                               ChatMessageMapper chatMessageMapper) {
        this.chatModelService = chatModelService;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Transactional
    public ChatResponseDto createConversation(ChatRequestDto requestDto, String answer) {
        log.info("Creating new conversation for agent: {}", requestDto.getAgentId());
        
        try {
            Conversation conversation = new Conversation();
            conversation.setAgentId(requestDto.getAgentId());
            
            String conversationName = chatModelService.createSummarize(
                requestDto.getAgentId(), 
                requestDto.getQuestion(), 
                CONVERSATION_NAME_MAX_LENGTH
            );
            conversation.setName(conversationName);
            
            Conversation savedConversation = conversationRepository.save(conversation);
            log.debug("Saved conversation with ID: {} and name: {}", savedConversation.getId(), conversationName);
            
            ChatMessage userMessage = ChatMessage.builder()
                .conversationId(savedConversation.getId())
                .content(requestDto.getQuestion())
                .agentId(requestDto.getAgentId())
                .type(MESSAGE_TYPE_USER)
                .build();
            chatMessageRepository.save(userMessage);
            log.debug("Saved user message for conversation: {}", savedConversation.getId());
            
            ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(savedConversation.getId())
                .content(answer)
                .agentId(requestDto.getAgentId())
                .type(MESSAGE_TYPE_ASSISTANT)
                .build();
            ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);
            log.debug("Saved assistant message for conversation: {}", savedConversation.getId());
            
            return buildChatResponse(savedAssistantMessage, savedConversation);
            
        } catch (Exception e) {
            log.error("Error creating conversation for agent: {}", requestDto.getAgentId(), e);
            throw new RuntimeException("Failed to create conversation", e);
        }
    }

    public List<ConversationResponseDto> listConversationsByAgentAndUser(String agentId, String userId) {
        log.info("Listing conversations for agent: {} and user: {}", agentId, userId);
        
        try {
            List<Conversation> conversations = conversationRepository
                .findAllByAgentIdAndCreatedByOrderByCreatedAtDesc(agentId, userId);
            
            log.debug("Found {} conversations for agent: {} and user: {}", 
                conversations.size(), agentId, userId);
            
            return conversations.stream()
                .map(conversationMapper::toDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error listing conversations for agent: {} and user: {}", agentId, userId, e);
            throw new RuntimeException("Failed to list conversations", e);
        }
    }

    public List<ChatMessageResponseDto> listMessagesByConversation(String conversationId) {
        log.info("Listing messages for conversation: {}", conversationId);
        
        try {
            conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
            
            List<ChatMessage> messages = chatMessageRepository
                .findAllByConversationIdOrderByCreatedAtAsc(conversationId);
            
            log.debug("Found {} messages for conversation: {}", messages.size(), conversationId);
            
            return messages.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error listing messages for conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to list messages", e);
        }
    }

    @Transactional
    public String createMessage(ChatRequestDto requestDto) {
        log.info("Creating new message for conversation: {}", requestDto.getConversationId());
        
        try {
            conversationRepository.findById(requestDto.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + requestDto.getConversationId()));
            
            ChatMessage userMessage = ChatMessage.builder()
                .conversationId(requestDto.getConversationId())
                .content(requestDto.getQuestion())
                .agentId(requestDto.getAgentId())
                .type(MESSAGE_TYPE_USER)
                .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(userMessage);
            log.debug("Saved user message with ID: {}", savedMessage.getId());
            
            return savedMessage.getId();
            
        } catch (Exception e) {
            log.error("Error creating message for conversation: {}", requestDto.getConversationId(), e);
            throw new RuntimeException("Failed to create message", e);
        }
    }

    @Transactional
    public ChatResponseDto addMessageToConversation(String conversationId, ChatRequestDto requestDto, String answer) {
        log.info("Adding message to conversation: {}", conversationId);
        
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
            
            ChatMessage userMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .content(requestDto.getQuestion())
                .agentId(requestDto.getAgentId())
                .type(MESSAGE_TYPE_USER)
                .build();
            chatMessageRepository.save(userMessage);
            log.debug("Saved user message for conversation: {}", conversationId);
            
            ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .content(answer)
                .agentId(requestDto.getAgentId())
                .type(MESSAGE_TYPE_ASSISTANT)
                .build();
            ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);
            log.debug("Saved assistant message for conversation: {}", conversationId);
            
            return buildChatResponse(savedAssistantMessage, conversation);
            
        } catch (Exception e) {
            log.error("Error adding message to conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to add message to conversation", e);
        }
    }

    @Transactional
    public void updateConversationName(String conversationId, String name) {
        log.info("Updating conversation name for ID: {}", conversationId);
        
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
            
            conversation.setName(name);
            conversationRepository.save(conversation);
            
            log.debug("Updated conversation name to: {}", name);
            
        } catch (Exception e) {
            log.error("Error updating conversation name for ID: {}", conversationId, e);
            throw new RuntimeException("Failed to update conversation name", e);
        }
    }

    /**
     * Creates a simple conversation without messages.
     * Used when creating a conversation through the REST API directly.
     * 
     * @param agentId The ID of the agent for this conversation
     * @param name Optional name for the conversation
     * @return DTO representation of the created conversation
     */
    @Transactional
    public ConversationResponseDto createSimpleConversation(String agentId, String name) {
        log.info("Creating simple conversation for agent: {}", agentId);
        
        try {
            Conversation conversation = new Conversation();
            conversation.setAgentId(agentId);
            conversation.setName(name != null && !name.isBlank() ? name : "New Conversation");
            
            Conversation savedConversation = conversationRepository.save(conversation);
            log.debug("Saved conversation with ID: {}", savedConversation.getId());
            
            return conversationMapper.toDto(savedConversation);
            
        } catch (Exception e) {
            log.error("Error creating simple conversation for agent: {}", agentId, e);
            throw new RuntimeException("Failed to create conversation", e);
        }
    }

    /**
     * Retrieves a single conversation by ID.
     * 
     * @param conversationId The unique identifier of the conversation
     * @return DTO representation of the conversation
     * @throws RuntimeException if conversation not found
     */
    public ConversationResponseDto getConversationById(String conversationId) {
        log.info("Getting conversation by ID: {}", conversationId);
        
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
            
            log.debug("Found conversation: {}", conversationId);
            
            return conversationMapper.toDto(conversation);
            
        } catch (Exception e) {
            log.error("Error getting conversation by ID: {}", conversationId, e);
            throw new RuntimeException("Failed to get conversation", e);
        }
    }

    private ChatResponseDto buildChatResponse(ChatMessage message, Conversation conversation) {
        log.debug("Building chat response for message: {} in conversation: {}", 
            message.getId(), conversation.getId());
        
        ChatMessageResponseDto messageDto = chatMessageMapper.toDto(message);
        ConversationResponseDto conversationDto = conversationMapper.toDto(conversation);
        
        return ChatResponseDto.builder()
            .message(messageDto)
            .conversation(conversationDto)
            .build();
    }

    public Flux<ServerSentEvent<String>> streamConversation(ChatRequestDto requestDto) {
        log.info("Starting conversation stream for agent: {}", requestDto.getAgentId());
        
        try {
            List<String> history = null;
            String summary = null;
            
            if (requestDto.getConversationId() != null && !requestDto.getConversationId().isBlank()) {
                List<ChatMessage> messages = chatMessageRepository
                    .findAllByConversationIdOrderByCreatedAtAsc(requestDto.getConversationId());
                
                history = messages.stream()
                    .map(msg -> {
                        String role = msg.getType() == MESSAGE_TYPE_USER ? "User" : "Assistant";
                        return role + ": " + msg.getContent();
                    })
                    .collect(Collectors.toList());
                
                log.debug("Loaded {} messages from conversation history", history.size());
            }
            
            StringBuilder completeResponse = new StringBuilder();
            
            Flux<String> streamResponse = chatModelService.call(requestDto, history, summary);
            
            return streamResponse
                .doOnNext(completeResponse::append)
                .map(chunk -> ServerSentEvent.<String>builder()
                    .data(chunk)
                    .build())
                .doOnComplete(() -> {
                    String answer = completeResponse.toString();
                    log.debug("Stream completed with response length: {}", answer.length());
                    
                    if (requestDto.getConversationId() == null || requestDto.getConversationId().isBlank()) {
                        createConversation(requestDto, answer);
                        log.info("Created new conversation after streaming");
                    } else {
                        addMessageToConversation(requestDto.getConversationId(), requestDto, answer);
                        log.info("Added messages to existing conversation after streaming");
                    }
                })
                .doOnError(error -> log.error("Error during conversation streaming", error));
                
        } catch (Exception e) {
            log.error("Error streaming conversation for agent: {}", requestDto.getAgentId(), e);
            return Flux.error(new RuntimeException("Failed to stream conversation", e));
        }
    }
}