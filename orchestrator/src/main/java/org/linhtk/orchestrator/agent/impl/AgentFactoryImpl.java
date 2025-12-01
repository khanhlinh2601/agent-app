package org.linhtk.orchestrator.agent.impl;

import com.google.genai.Client;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.linhtk.orchestrator.agent.AgentFactory;
import org.linhtk.orchestrator.model.agent.Agent;
import org.linhtk.orchestrator.model.agent.AgentTools;
import org.linhtk.orchestrator.repository.AgentRepository;
import org.linhtk.orchestrator.repository.AgentToolsRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.i18n.LocaleContextHolder;

public class AgentFactoryImpl implements AgentFactory {
    private final AgentRepository agentRepository;
    private final AgentToolsRepository agentToolsRepository;

    public AgentFactoryImpl(AgentRepository agentRepository, AgentToolsRepository agentToolsRepository) {
        this.agentRepository = agentRepository;
        this.agentToolsRepository = agentToolsRepository;
    }

    @Override
    public ChatClient createAgent(String agentId) {

        var agent = agentRepository.findById(agentId)
            .orElseThrow(() ->
                new RuntimeException("Agent with id " + agentId + " not found"));

        String provider = agent.getProviderName();

        return switch (provider) {
            case "OpenAi" -> createOpenAIAgent(agent);
            case "GoogleAi" -> createGoogleAiAgent(agent);
            default -> throw new RuntimeException("Unsupported provider: " + provider);
        };
    }


    @Override
    public ChatClient createBasicAgent(String instructions) {
        return null;
    }

    private ChatClient createOpenAIAgent(Agent agent) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(agent.providerModelName)
            .maxTokens(1000)
            .build();
        OpenAiApi openAiApi = OpenAiApi.builder()
            .apiKey(agent.providerApiKey)
            .baseUrl(agent.providerEndpoint)
            .build();
        ChatModel model = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(options)
            .build();
        return ChatClient.builder(model)
            .defaultSystem(agent.instructions)
            .defaultTools(getAgentToolsByAgentId(agent.id))
            .build();
    }

    private ChatClient createGoogleAiAgent(Agent agent) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
            .model(agent.providerModelName)
            .build();
        Client genAiClient = Client.builder()
            .apiKey(agent.providerApiKey)
            .build();
        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
            .defaultOptions(options)
            .genAiClient(genAiClient)
            .build();
        return ChatClient.builder(model)
            .defaultSystem(agent.instructions)
            .defaultTools(getAgentToolsByAgentId(agent.getId()))
            .build();
    }

    private List<String> getAgentToolsByAgentId(String agentId) {
        List<AgentTools> agentTools = this.agentToolsRepository.findAllByAgentId(agentId);
        List<String> tools = agentTools.stream().filter(
            a -> a.isEnabled
        ).map(AgentTools::getName).collect(Collectors.toList());
        tools.add(LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString());
        return tools;
    }

}
