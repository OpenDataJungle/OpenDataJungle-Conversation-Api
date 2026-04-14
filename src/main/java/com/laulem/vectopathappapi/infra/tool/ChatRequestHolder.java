package com.laulem.vectopathappapi.infra.tool;

import com.laulem.vectopathappapi.business.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ChatRequestHolder {

    private final ThreadLocal<State> context = ThreadLocal.withInitial(State::new);

    public void addToolResult(String toolId, Map<String, Object> result) {
        context.get().toolResults.add(new ToolResult(toolId, result));
    }

    public List<ToolResult> getToolResults() {
        return Collections.unmodifiableList(context.get().toolResults);
    }

    public void setResourceIds(List<UUID> resourceIds) {
        context.get().resourceIds = resourceIds;
    }

    public List<UUID> getResourceIds() {
        return context.get().resourceIds;
    }

    public void clear() {
        context.remove();
    }

    private static class State {
        final List<ToolResult> toolResults = new ArrayList<>();
        List<UUID> resourceIds;
    }
}

