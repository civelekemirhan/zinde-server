package com.wexec.zinde_server.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainerSpecialty {

    PERSONAL_TRAINING("Kişisel Antrenörlük"),
    NUTRITION("Beslenme & Diyet"),
    YOGA_PILATES("Yoga & Pilates"),
    CROSSFIT("Crossfit"),
    BODYBUILDING("Bodybuilding"),
    FUNCTIONAL_TRAINING("Fonksiyonel Antrenman"),
    RUNNING_ATHLETICS("Koşu & Atletizm"),
    SWIMMING("Yüzme"),
    MARTIAL_ARTS("Dövüş Sporları"),
    REHABILITATION("Rehabilitasyon");

    private final String displayName;
}
