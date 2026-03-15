package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.Gender;
import com.wexec.zinde_server.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
    private Gender gender;
    private UserRole role;
    private long postCount;
    private long friendCount;

    /**
     * Görüntüleyen kullanıcının bu profile göre durumu:
     * null             → kendi profili
     * NOT_FOLLOWING    → hiç ilişki yok
     * PENDING_SENT     → istek gönderildi, bekleniyor
     * PENDING_RECEIVED → bu kişi sana istek gönderdi
     * FRIENDS          → arkadaşlar (ACCEPTED)
     */
    private String followStatus;
    private boolean me;
    private LocalDateTime createdAt;

    /** Sadece role == TRAINER olan kullanıcılarda dolu gelir */
    private TrainerProfileResponse trainerProfile;
}
