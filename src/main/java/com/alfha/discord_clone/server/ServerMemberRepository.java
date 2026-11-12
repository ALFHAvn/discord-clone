package com.alfha.discord_clone.server;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServerMemberRepository extends JpaRepository<ServerMember, Long> {
    @EntityGraph(attributePaths = "user")
    List<ServerMember> findByServerIdOrderByUserIdAsc(Long serverId);

    @EntityGraph(attributePaths = "user")
    Optional<ServerMember> findByServerIdAndUserId(Long serverId, Long userId);

    boolean existsByServerIdAndUserId(Long serverId, Long userId);

    @Modifying
    @Query("delete from ServerMember member where member.server.id = :serverId and member.user.id = :userId")
    int deleteByServerIdAndUserId(@Param("serverId") Long serverId, @Param("userId") Long userId);

    @Modifying
    @Query("delete from ServerMember member where member.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from ServerMember member where member.server.id = :serverId")
    void deleteByServerId(@Param("serverId") Long serverId);
}
