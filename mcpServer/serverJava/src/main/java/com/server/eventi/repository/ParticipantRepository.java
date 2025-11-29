package com.server.eventi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.eventi.model.ParticipantEntity;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {
    List<ParticipantEntity> findByEventId(UUID eventId);
}
