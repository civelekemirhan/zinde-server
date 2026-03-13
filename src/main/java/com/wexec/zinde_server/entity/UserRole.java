package com.wexec.zinde_server.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ATHLETE("Sporcu"),
    TRAINER("Antrenör"),
    GYM("Spor Salonu"),
    BRAND("Spor Markası"),
    ADMIN("Yönetici");

    private final String displayName;
}
