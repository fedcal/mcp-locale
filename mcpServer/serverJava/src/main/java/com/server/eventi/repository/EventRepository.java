package com.server.eventi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.eventi.model.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
}
