package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.Search;

import java.util.List;
import java.util.UUID;

public interface SearchService {
    List<Search> search(String query, int limit, List<UUID> resourceIds);
}

