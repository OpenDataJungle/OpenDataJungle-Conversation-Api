package com.opendatajungle.conversation.api.infra.repository;

import com.opendatajungle.conversation.api.infra.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {
    List<ConversationEntity> findAllByUserId(String userId);

    Optional<ConversationEntity> findByIdAndUserId(UUID id, String userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConversationEntity c SET c.lastMessageAt = :lastMessageAt WHERE c.id = :id")
    void updateLastMessageAt(@Param("id") UUID id, @Param("lastMessageAt") LocalDateTime lastMessageAt);
}
