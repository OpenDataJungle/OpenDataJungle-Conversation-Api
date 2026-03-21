package com.laulem.vectopathappapi.infra.repository;

import com.laulem.vectopathappapi.infra.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {
    List<ConversationEntity> findAllByUserId(String userId);

    Optional<ConversationEntity> findByIdAndUserId(UUID id, String userId);
}
