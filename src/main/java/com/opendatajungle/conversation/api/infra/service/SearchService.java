package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.conversation.api.infra.model.Search;

import java.util.List;
import java.util.UUID;

public interface SearchService {
    List<Search> search(String query, int limit, List<UUID> resourceIds);
}
