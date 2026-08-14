package com.opendatajungle.conversation.api.client.config;

import com.opendatajungle.conversation.api.business.exception.DownloadInterruptedException;
import com.opendatajungle.conversation.api.business.exception.HttpDownloadException;
import com.opendatajungle.conversation.api.business.exception.ResourceDeletionException;
import com.opendatajungle.commons.client.dto.GeneralResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DomainExceptionHandler.class);

    @ExceptionHandler(HttpDownloadException.class)
    public ResponseEntity<GeneralResponseException> handleHttpDownloadException(HttpDownloadException ex, HttpServletRequest request) {
        logger.error("HttpDownloadException: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        GeneralResponseException response = new GeneralResponseException(
                "HTTP_DOWNLOAD_ERROR",
                ex.getMessage(),
                buildPath(request),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DownloadInterruptedException.class)
    public ResponseEntity<GeneralResponseException> handleDownloadInterruptedException(DownloadInterruptedException ex, HttpServletRequest request) {
        logger.error("DownloadInterruptedException: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        GeneralResponseException response = new GeneralResponseException(
                "DOWNLOAD_INTERRUPTED",
                ex.getMessage(),
                buildPath(request),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResourceDeletionException.class)
    public ResponseEntity<GeneralResponseException> handleVectorStoreDeletionException(ResourceDeletionException ex, HttpServletRequest request) {
        logger.error("VectorStoreDeletionException: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        GeneralResponseException response = new GeneralResponseException(
                "VECTOR_STORE_DELETION_ERROR",
                ex.getMessage(),
                buildPath(request),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String buildPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
