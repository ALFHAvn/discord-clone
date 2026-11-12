package com.alfha.discord_clone.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    boolean existsByAuthorId(Long authorId);

    boolean existsByChannelId(Long channelId);

    Optional<Message> findByIdAndChannelId(Long id, Long channelId);

    Optional<Message> findByIdAndChannelIdAndChannelServerId(Long id, Long channelId, Long serverId);

    Slice<Message> findByChannelIdAndIdGreaterThanOrderByIdAsc(Long channelId, long afterId, Pageable pageable);
}
