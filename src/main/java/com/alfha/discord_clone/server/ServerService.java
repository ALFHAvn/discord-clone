package com.alfha.discord_clone.server;

import com.alfha.discord_clone.channel.ChannelRepository;
import com.alfha.discord_clone.user.User;
import com.alfha.discord_clone.user.UserNotFoundException;
import com.alfha.discord_clone.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public ServerService(ServerRepository serverRepository, ServerMemberRepository serverMemberRepository,
                         UserRepository userRepository, ChannelRepository channelRepository) {
        this.serverRepository = serverRepository;
        this.serverMemberRepository = serverMemberRepository;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Transactional
    public ServerDto createServer(CreateServerRequest request) {
        User owner = userRepository.findForUpdateById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException(request.ownerId()));
        Server server = serverRepository.save(new Server(request.name(), owner));
        serverMemberRepository.save(new ServerMember(server, owner));
        return toDto(server);
    }

    @Transactional(readOnly = true)
    public List<ServerDto> getAllServers() {
        return serverRepository.findAll(Sort.by("id"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServerDto getServerById(Long id) {
        return serverRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ServerNotFoundException(id));
    }

    @Transactional
    public ServerDto updateServer(Long id, UpdateServerRequest request) {
        Server server = findServerForUpdate(id);
        server.rename(request.name());
        return toDto(server);
    }

    @Transactional
    public void deleteServer(Long id) {
        Server server = findServerForUpdate(id);
        if (channelRepository.existsByServerId(id)) {
            throw new ServerHasChannelsException();
        }
        serverMemberRepository.deleteByServerId(id);
        serverRepository.delete(server);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<ServerMemberDto> getMembers(Long serverId) {
        if (!serverRepository.existsById(serverId)) {
            throw new ServerNotFoundException(serverId);
        }
        return serverMemberRepository.findByServerIdOrderByUserIdAsc(serverId)
                .stream()
                .map(this::toMemberDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServerMemberDto getMember(Long serverId, Long userId) {
        return serverMemberRepository.findByServerIdAndUserId(serverId, userId)
                .map(this::toMemberDto)
                .orElseThrow(() -> new ServerMemberNotFoundException(serverId, userId));
    }

    @Transactional
    public ServerMemberDto addMember(Long serverId, Long userId) {
        Server server = findServerForUpdate(serverId);
        User user = userRepository.findForUpdateById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (serverMemberRepository.existsByServerIdAndUserId(serverId, userId)) {
            throw new ServerMembershipConflictException("The user is already a member of this server.");
        }
        return toMemberDto(serverMemberRepository.save(new ServerMember(server, user)));
    }

    @Transactional
    public void removeMember(Long serverId, Long userId) {
        Server server = findServerForUpdate(serverId);
        if (server.getOwner().getId().equals(userId)) {
            throw new ServerMembershipConflictException("The server owner must remain a member.");
        }
        if (serverMemberRepository.deleteByServerIdAndUserId(serverId, userId) == 0) {
            throw new ServerMemberNotFoundException(serverId, userId);
        }
    }

    private Server findServerForUpdate(Long id) {
        return serverRepository.findForUpdateById(id)
                .orElseThrow(() -> new ServerNotFoundException(id));
    }

    private ServerDto toDto(Server server) {
        return new ServerDto(server.getId(), server.getName(), server.getOwner().getId());
    }

    private ServerMemberDto toMemberDto(ServerMember member) {
        User user = member.getUser();
        return new ServerMemberDto(user.getId(), user.getUsername());
    }
}
