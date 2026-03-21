package com.laulem.vectopathappapi.business.exception;

import java.util.UUID;

public class ResourceDeletionException extends RuntimeException {
    public ResourceDeletionException(UUID resourceId, Throwable cause) {
        super("Failed to delete resource: " + resourceId, cause);
    }
}
