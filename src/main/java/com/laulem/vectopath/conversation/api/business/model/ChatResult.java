package com.laulem.vectopath.conversation.api.business.model;

import java.util.List;

public record ChatResult(String reply, List<ToolResult> toolResults) {
}
