package com.alfha.discord_clone.message;

import com.alfha.discord_clone.channel.Channel;
import com.alfha.discord_clone.channel.ChannelNotFoundException;
import com.alfha.discord_clone.channel.ChannelRepository;
import com.alfha.discord_clone.user.User;
import com.alfha.discord_clone.user.UserNotFoundException;
import com.alfha.discord_clone.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, ChannelRepository channelRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageDto createMessage(Long serverId, Long channelId, CreateMessageRequest request) {
        Channel channel = findChannelForUpdate(serverId, channelId);
        User author = userRepository.findForUpdateById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return toDto(messageRepository.save(new Message(channel, author, request.content(), createdAt)));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public MessagePage getMessages(Long serverId, Long channelId, long afterId, int limit) {
        if (!channelRepository.existsByIdAndServerId(channelId, serverId)) {
            throw new ChannelNotFoundException(serverId, channelId);
        }
        Slice<Message> page = messageRepository.findByChannelIdAndIdGreaterThanOrderByIdAsc(
                channelId, afterId, PageRequest.of(0, limit));
        List<MessageDto> messages = page.map(this::toDto).getContent();
        Long nextAfterId = page.hasNext() ? messages.getLast().id() : null;
        return new MessagePage(messages, nextAfterId);
    }

    @Transactional(readOnly = true)
    public MessageDto getMessage(Long serverId, Long channelId, Long messageId) {
        return messageRepository.findByIdAndChannelIdAndChannelServerId(messageId, channelId, serverId)
                .map(this::toDto)
                .orElseThrow(() -> new MessageNotFoundException(channelId, messageId));
    }

    @Transactional
    public MessageDto updateMessage(Long serverId, Long channelId, Long messageId, UpdateMessageRequest request) {
        Message message = findMessageForUpdate(serverId, channelId, messageId);
        message.editContent(request.content());
        return toDto(message);
    }

    @Transactional
    public void deleteMessage(Long serverId, Long channelId, Long messageId) {
        messageRepository.delete(findMessageForUpdate(serverId, channelId, messageId));
    }

    private Channel findChannelForUpdate(Long serverId, Long channelId) {
        return channelRepository.findForUpdateByIdAndServerId(channelId, serverId)
                .orElseThrow(() -> new ChannelNotFoundException(serverId, channelId));
    }

    private Message findMessageForUpdate(Long serverId, Long channelId, Long messageId) {
        findChannelForUpdate(serverId, channelId);
        return messageRepository.findByIdAndChannelId(messageId, channelId)
                .orElseThrow(() -> new MessageNotFoundException(channelId, messageId));
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(message.getId(), message.getChannel().getId(), message.getAuthor().getId(),
                message.getContent(), message.getCreatedAt());
    }
}
