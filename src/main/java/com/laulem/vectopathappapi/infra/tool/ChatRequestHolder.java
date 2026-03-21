package com.laulem.vectopathappapi.infra.tool;

import com.laulem.vectopathappapi.business.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ChatRequestHolder {

    private final ThreadLocal<State> context = ThreadLocal.withInitial(State::new);

    public void addToolResult(String toolId, Map<String, Object> result) {
        context.get().toolResults.add(new ToolResult(toolId, result));
    }

    public List<ToolResult> getToolResults() {
        return Collections.unmodifiableList(context.get().toolResults);
    }

    public void clear() {
        context.remove();
    }

    private static class State {
        final List<ToolResult> toolResults = new ArrayList<>();
    }
}

