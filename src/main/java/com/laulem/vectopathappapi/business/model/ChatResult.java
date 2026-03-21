package com.laulem.vectopathappapi.business.model;

import java.util.List;

public record ChatResult(String reply, List<ToolResult> toolResults) {
}
