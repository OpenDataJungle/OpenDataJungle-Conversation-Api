package com.laulem.vectopathappapi.business.model;

import java.util.Map;

public record ToolResult(String id, String query, Map<String, Object> result) {

}
