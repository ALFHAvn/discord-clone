package com.alfha.discord_clone.channel;

import com.alfha.discord_clone.message.MessageRepository;
import com.alfha.discord_clone.server.Server;
import com.alfha.discord_clone.server.ServerNotFoundException;
import com.alfha.discord_clone.server.ServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final MessageRepository messageRepository;

    public ChannelService(ChannelRepository channelRepository, ServerRepository serverRepository,
                          MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.serverRepository = serverRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChannelDto createChannel(Long serverId, CreateChannelRequest request) {
        Server server = serverRepository.findForUpdateById(serverId)
                .orElseThrow(() -> new ServerNotFoundException(serverId));
        return toDto(channelRepository.save(new Channel(server, request.name(), request.type())));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<ChannelDto> getChannels(Long serverId) {
        if (!serverRepository.existsById(serverId)) {
            throw new ServerNotFoundException(serverId);
        }
        return channelRepository.findByServerIdOrderByIdAsc(serverId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChannelDto getChannel(Long serverId, Long channelId) {
        return channelRepository.findByIdAndServerId(channelId, serverId)
                .map(this::toDto)
                .orElseThrow(() -> new ChannelNotFoundException(serverId, channelId));
    }

    @Transactional
    public ChannelDto updateChannel(Long serverId, Long channelId, UpdateChannelRequest request) {
        Channel channel = findChannelForUpdate(serverId, channelId);
        channel.rename(request.name());
        return toDto(channel);
    }

    @Transactional
    public void deleteChannel(Long serverId, Long channelId) {
        Channel channel = findChannelForUpdate(serverId, channelId);
        if (messageRepository.existsByChannelId(channelId)) {
            throw new ChannelHasMessagesException();
        }
        channelRepository.delete(channel);
    }

    private Channel findChannelForUpdate(Long serverId, Long channelId) {
        return channelRepository.findForUpdateByIdAndServerId(channelId, serverId)
                .orElseThrow(() -> new ChannelNotFoundException(serverId, channelId));
    }

    private ChannelDto toDto(Channel channel) {
        return new ChannelDto(channel.getId(), channel.getServer().getId(), channel.getName(), channel.getType());
    }
}
