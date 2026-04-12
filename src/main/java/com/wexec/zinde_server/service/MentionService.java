package com.wexec.zinde_server.service;

import com.wexec.zinde_server.entity.Mention;
import com.wexec.zinde_server.entity.Post;
import com.wexec.zinde_server.entity.PostComment;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.repository.FollowRequestRepository;
import com.wexec.zinde_server.repository.MentionRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w.]+)");

    private final UserRepository userRepository;
    private final FollowRequestRepository followRequestRepository;
    private final MentionRepository mentionRepository;
    private final FcmService fcmService;

    /**
     * Post caption'ındaki @username mention'larını işler.
     * Takip ilişkisi olan (birbirini takip eden) kullanıcılar etiketlenebilir.
     */
    @Transactional
    public void processPostMentions(User mentionedBy, Post post, String text) {
        if (text == null || text.isBlank()) return;

        for (String username : extractUsernames(text)) {
            if (username.equalsIgnoreCase(mentionedBy.getUsername())) continue; // kendini etiketleyemez

            userRepository.findByUsername(username).ifPresent(target -> {
                boolean connected = followRequestRepository.existsByFromUserIdAndToUserId(mentionedBy.getId(), target.getId())
                        || followRequestRepository.existsByFromUserIdAndToUserId(target.getId(), mentionedBy.getId());
                if (!connected) {
                    log.debug("Mention atlandı: {} takip ilişkisi yok → {}", mentionedBy.getUsername(), username);
                    return;
                }
                try {
                    mentionRepository.save(Mention.builder()
                            .mentionedBy(mentionedBy)
                            .mentionedUser(target)
                            .post(post)
                            .build());
                    sendMentionNotification(mentionedBy, target, post.getId(), null);
                } catch (Exception e) {
                    log.warn("Mention kaydedilemedi (post={}, user={}): {}", post.getId(), username, e.getMessage());
                }
            });
        }
    }

    /**
     * Yorum içeriğindeki @username mention'larını işler.
     */
    @Transactional
    public void processCommentMentions(User mentionedBy, PostComment comment, String text) {
        if (text == null || text.isBlank()) return;

        for (String username : extractUsernames(text)) {
            if (username.equalsIgnoreCase(mentionedBy.getUsername())) continue;

            userRepository.findByUsername(username).ifPresent(target -> {
                boolean connected = followRequestRepository.existsByFromUserIdAndToUserId(mentionedBy.getId(), target.getId())
                        || followRequestRepository.existsByFromUserIdAndToUserId(target.getId(), mentionedBy.getId());
                if (!connected) {
                    log.debug("Mention atlandı: {} takip ilişkisi yok → {}", mentionedBy.getUsername(), username);
                    return;
                }
                try {
                    mentionRepository.save(Mention.builder()
                            .mentionedBy(mentionedBy)
                            .mentionedUser(target)
                            .comment(comment)
                            .build());
                    sendMentionNotification(mentionedBy, target, null, comment.getId());
                } catch (Exception e) {
                    log.warn("Mention kaydedilemedi (comment={}, user={}): {}", comment.getId(), username, e.getMessage());
                }
            });
        }
    }

    private void sendMentionNotification(User from, User to, Long postId, Long commentId) {
        if (to.getFcmToken() == null || to.getFcmToken().isBlank()) return;

        String body = postId != null
                ? from.getUsername() + " seni bir gönderide etiketledi."
                : from.getUsername() + " seni bir yorumda etiketledi.";

        var data = new java.util.HashMap<String, String>();
        data.put("type", "MENTION");
        data.put("mentionedBy", from.getUsername());
        if (postId != null) data.put("postId", postId.toString());
        if (commentId != null) data.put("commentId", commentId.toString());

        fcmService.sendToToken(to.getFcmToken(), "Etiketlendin!", body, data);
    }

    private Set<String> extractUsernames(String text) {
        Set<String> usernames = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }
}
