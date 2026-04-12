package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.Gender;
import com.wexec.zinde_server.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
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
    private long followerCount;
    private long followingCount;
    private boolean isFollowing;
    private boolean me;
    private LocalDateTime createdAt;
    private TrainerProfileResponse trainerProfile;
    private List<PackagePurchaseSummaryResponse> activePackages;
}
