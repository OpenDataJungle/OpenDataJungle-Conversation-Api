package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.Search;

import java.util.List;

public interface SearchService {
    List<Search> search(String query, int limit);
}

