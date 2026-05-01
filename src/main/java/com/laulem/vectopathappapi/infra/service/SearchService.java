package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.infra.model.Search;

import java.util.List;
import java.util.UUID;

public interface SearchService {
    List<Search> search(String query, int limit, List<UUID> resourceIds);
}
