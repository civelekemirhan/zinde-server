package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.*;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;

    @Transactional
    public Poll createPoll(Post post, String title, List<String> optionTexts, LocalDateTime expiresAt) {
        if (title == null || title.isBlank()) {
            throw new AppException("POLL_TITLE_REQUIRED", "Anket başlığı boş olamaz.");
        }
        if (optionTexts == null || optionTexts.size() < 2) {
            throw new AppException("POLL_MIN_OPTIONS", "Anket en az 2 seçenek içermeli.");
        }
        if (optionTexts.size() > 10) {
            throw new AppException("POLL_MAX_OPTIONS", "Anket en fazla 10 seçenek içerebilir.");
        }
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            throw new AppException("POLL_INVALID_EXPIRY", "Bitiş tarihi gelecekte olmalıdır.");
        }

        Poll poll = Poll.builder()
                .post(post)
                .title(title)
                .expiresAt(expiresAt)
                .build();
        pollRepository.save(poll);

        for (String text : optionTexts) {
            if (text == null || text.isBlank()) continue;
            PollOption option = PollOption.builder()
                    .poll(poll)
                    .text(text.trim())
                    .voteCount(0)
                    .build();
            pollOptionRepository.save(option);
            poll.getOptions().add(option);
        }

        return poll;
    }

    @Transactional
    public PollResponse vote(UUID userId, Long pollId, Long optionId) {
        User user = getUser(userId);
        Poll poll = getPoll(pollId);

        if (poll.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("POLL_EXPIRED", "Anket süresi dolmuştur.");
        }
        if (pollVoteRepository.existsByPollIdAndUserId(pollId, userId)) {
            throw new AppException("ALREADY_VOTED", "Bu ankete zaten oy verdiniz.");
        }

        PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new AppException("OPTION_NOT_FOUND", "Seçenek bulunamadı.", HttpStatus.NOT_FOUND));

        if (!option.getPoll().getId().equals(pollId)) {
            throw new AppException("INVALID_OPTION", "Bu seçenek bu ankete ait değil.");
        }

        pollVoteRepository.save(PollVote.builder()
                .poll(poll)
                .option(option)
                .user(user)
                .build());

        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);

        return toPollResponse(poll, userId);
    }

    public List<PollResponse> getMyPolls(UUID userId) {
        return pollRepository.findByUserId(userId)
                .stream()
                .map(poll -> toPollResponse(poll, userId))
                .collect(Collectors.toList());
    }

    public PollResultResponse getPollResults(UUID userId, Long pollId) {
        Poll poll = getPoll(pollId);
        if (!poll.getPost().getUser().getId().equals(userId)) {
            throw new AppException("FORBIDDEN", "Bu anketi görüntüleme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        return toPollResultResponse(poll);
    }

    public PollResponse getPollByPostId(Long postId, UUID currentUserId) {
        return pollRepository.findByPostId(postId)
                .map(poll -> toPollResponse(poll, currentUserId))
                .orElse(null);
    }

    public PollResponse toPollResponse(Poll poll, UUID currentUserId) {
        List<PollOption> options = poll.getOptions().isEmpty()
                ? pollOptionRepository.findByPollId(poll.getId())
                : poll.getOptions();

        int totalVotes = options.stream().mapToInt(PollOption::getVoteCount).sum();

        Optional<PollVote> myVote = pollVoteRepository.findByPollIdAndUserId(poll.getId(), currentUserId);
        Long votedOptionId = myVote.map(v -> v.getOption().getId()).orElse(null);

        List<PollOptionResponse> optionResponses = options.stream()
                .map(opt -> PollOptionResponse.builder()
                        .id(opt.getId())
                        .text(opt.getText())
                        .voteCount(opt.getVoteCount())
                        .percentage(totalVotes > 0 ? (double) opt.getVoteCount() / totalVotes * 100 : 0.0)
                        .votedByMe(myVote.map(v -> v.getOption().getId().equals(opt.getId())).orElse(false))
                        .build())
                .collect(Collectors.toList());

        return PollResponse.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .expiresAt(poll.getExpiresAt())
                .expired(poll.getExpiresAt().isBefore(LocalDateTime.now()))
                .totalVotes(totalVotes)
                .votedOptionId(votedOptionId)
                .options(optionResponses)
                .build();
    }

    private PollResultResponse toPollResultResponse(Poll poll) {
        List<PollOption> options = poll.getOptions().isEmpty()
                ? pollOptionRepository.findByPollId(poll.getId())
                : poll.getOptions();

        int totalVotes = options.stream().mapToInt(PollOption::getVoteCount).sum();

        List<PollOptionResultResponse> optionResults = options.stream()
                .map(opt -> {
                    List<PollVoterResponse> voters = pollVoteRepository.findByOptionId(opt.getId())
                            .stream()
                            .map(vote -> PollVoterResponse.builder()
                                    .userId(vote.getUser().getId())
                                    .username(vote.getUser().getUsername())
                                    .votedAt(vote.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());
                    return PollOptionResultResponse.builder()
                            .id(opt.getId())
                            .text(opt.getText())
                            .voteCount(opt.getVoteCount())
                            .percentage(totalVotes > 0 ? (double) opt.getVoteCount() / totalVotes * 100 : 0.0)
                            .voters(voters)
                            .build();
                })
                .collect(Collectors.toList());

        return PollResultResponse.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .expiresAt(poll.getExpiresAt())
                .expired(poll.getExpiresAt().isBefore(LocalDateTime.now()))
                .totalVotes(totalVotes)
                .options(optionResults)
                .build();
    }

    private Poll getPoll(Long pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new AppException("POLL_NOT_FOUND", "Anket bulunamadı.", HttpStatus.NOT_FOUND));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
