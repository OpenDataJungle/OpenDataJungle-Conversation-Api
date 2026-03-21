package com.laulem.vectopathappapi.infra.repository;

import com.laulem.vectopathappapi.infra.entity.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationMessageJpaRepository extends JpaRepository<ConversationMessageEntity, UUID> {
    List<ConversationMessageEntity> findAllByConversationIdAndInContextTrueOrderByCreatedAtAsc(UUID conversationId);

    List<ConversationMessageEntity> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<ConversationMessageEntity> findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(UUID conversationId);

    @Query("SELECT DISTINCT m.conversationId FROM ConversationMessageEntity m")
    List<UUID> findDistinctConversationIds();

    long countByConversationIdAndInContextTrue(UUID conversationId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConversationMessageEntity m SET m.inContext = false WHERE m.conversationId = :conversationId and m.id NOT IN :ids")
    void setAllOutOfContextButIds(@Param("conversationId") UUID conversationId, @Param("ids") List<UUID> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConversationMessageEntity m SET m.inContext = false WHERE m.conversationId = :conversationId")
    void setAllOutOfContext(@Param("conversationId") UUID conversationId);
}
