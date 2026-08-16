package com.opendatajungle.conversation.api.infra.tool;

import com.opendatajungle.conversation.api.business.model.ToolResult;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A holder for storing tool results and resource IDs during a chat request lifecycle.
 * It uses ThreadLocal to maintain state specific to the current thread, ensuring that data is isolated per request and can be accessed by different components involved in processing the chat request.
 */
@Component
@RequestScope
public class ChatRequestHolder {

    private final ThreadLocal<State> context = ThreadLocal.withInitial(State::new);

    public void addToolResult(String toolId, String query, Map<String, Object> result) {
        context.get().toolResults.add(new ToolResult(toolId, query, result));
    }

    public List<ToolResult> getToolResults() {
        return Collections.unmodifiableList(context.get().toolResults);
    }

    public List<UUID> getResourceIds() {
        return context.get().resourceIds;
    }

    public void setResourceIds(List<UUID> resourceIds) {
        context.get().resourceIds = resourceIds;
    }

    public void clear() {
        context.remove();
    }

    private static class State {
        final List<ToolResult> toolResults = new ArrayList<>();
        List<UUID> resourceIds;
    }
}

