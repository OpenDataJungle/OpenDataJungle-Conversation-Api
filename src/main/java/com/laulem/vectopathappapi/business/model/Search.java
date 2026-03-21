package com.laulem.vectopathappapi.business.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Search {
    private UUID vectorId;

    private UUID resourceId;

    private String resourceName;

    private String content;

    private String contentType;

    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Double similarityScore;

    public UUID getVectorId() {
        return vectorId;
    }

    public void setVectorId(final UUID vectorId) {
        this.vectorId = vectorId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(final Double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
