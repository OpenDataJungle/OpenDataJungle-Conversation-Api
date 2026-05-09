package com.laulem.vectopathappapi.infra.service.chatpreprocessor;

import com.laulem.vectopathappapi.infra.model.ChatContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@ConditionalOnMissingBean(ChatPreProcessingOrchestrator.class)
@Slf4j
public class ChatPreProcessingOrchestratorImpl implements ChatPreProcessingOrchestrator {

    private final List<ChatPreProcessor> processors;

    public ChatPreProcessingOrchestratorImpl(List<ChatPreProcessor> processors) {
        this.processors = processors.stream()
                .sorted(Comparator.comparingInt(ChatPreProcessor::getOrder))
                .toList();
    }

    @Override
    public ChatContext run(ChatContext initialChatContext) {
        ChatContext chatContext = initialChatContext;
        for (ChatPreProcessor processor : processors) {
            chatContext = processor.process(chatContext);
        }
        return chatContext;
    }
}
