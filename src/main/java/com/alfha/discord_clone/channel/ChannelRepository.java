package com.alfha.discord_clone.channel;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    boolean existsByServerId(Long serverId);

    boolean existsByIdAndServerId(Long id, Long serverId);

    List<Channel> findByServerIdOrderByIdAsc(Long serverId);

    Optional<Channel> findByIdAndServerId(Long id, Long serverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Channel> findForUpdateByIdAndServerId(Long id, Long serverId);
}
