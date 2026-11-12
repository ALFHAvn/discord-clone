package com.alfha.discord_clone.server;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {
    boolean existsByOwnerId(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Server> findForUpdateById(Long id);
}
