package com.opendatajungle.conversation.api.business.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String resourceType, String id) {
        super(resourceType + " not found with id: " + id);
    }
}
