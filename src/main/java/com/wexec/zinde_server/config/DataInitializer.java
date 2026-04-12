package com.wexec.zinde_server.config;

import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final TrainerPackageRepository trainerPackageRepository;
    private final FollowRequestRepository followRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("DataInitializer: veri zaten mevcut, atlanıyor.");
            return;
        }

        log.info("DataInitializer: seed verisi oluşturuluyor...");

        // ── Kullanıcılar ─────────────────────────────────────────────────────

        User trainer1 = createUser("Ahmet", "Yılmaz", "ahmet_trainer", "ahmet@zinde.app", "test1234", Gender.MALE, UserRole.TRAINER);
        User trainer2 = createUser("Selin", "Kaya", "selin_trainer", "selin@zinde.app", "test1234", Gender.FEMALE, UserRole.TRAINER);
        User athlete1 = createUser("Emirhan", "Civelek", "emirhan", "emirhan@zinde.app", "test1234", Gender.MALE, UserRole.ATHLETE);
        User athlete2 = createUser("Zeynep", "Arslan", "zeynep_fit", "zeynep@zinde.app", "test1234", Gender.FEMALE, UserRole.ATHLETE);
        User athlete3 = createUser("Burak", "Demir", "burak_lifts", "burak@zinde.app", "test1234", Gender.MALE, UserRole.ATHLETE);

        userRepository.saveAll(List.of(trainer1, trainer2, athlete1, athlete2, athlete3));

        // ── Trainer Paketleri ─────────────────────────────────────────────────

        trainerPackageRepository.saveAll(List.of(
                TrainerPackage.builder()
                        .trainer(trainer1)
                        .name("Başlangıç Paketi")
                        .description("Sıfırdan spora başlayanlar için 30 günlük temel antrenman programı.")
                        .price(new BigDecimal("499.00"))
                        .durationDays(30)
                        .totalLessons(12)
                        .active(true)
                        .build(),
                TrainerPackage.builder()
                        .trainer(trainer1)
                        .name("İleri Seviye Paketi")
                        .description("Deneyimli sporcular için yoğun 60 günlük program.")
                        .price(new BigDecimal("899.00"))
                        .durationDays(60)
                        .totalLessons(24)
                        .active(true)
                        .build(),
                TrainerPackage.builder()
                        .trainer(trainer2)
                        .name("Kadın Fitness Paketi")
                        .description("Kadınlara özel beslenme + antrenman 45 günlük program.")
                        .price(new BigDecimal("649.00"))
                        .durationDays(45)
                        .totalLessons(18)
                        .active(true)
                        .build()
        ));

        // ── Postlar ───────────────────────────────────────────────────────────

        Post post1 = postRepository.save(Post.builder()
                .user(trainer1)
                .imageKey("seed/trainer1_post1.jpg")
                .caption("Bugün harika bir antrenman seansı! Deadlift PR kırdım 💪 #fitness #powerlifting")
                .moderationStatus(ModerationStatus.APPROVED)
                .likeCount(42)
                .commentCount(0)
                .build());

        Post post2 = postRepository.save(Post.builder()
                .user(trainer2)
                .imageKey("seed/trainer2_post1.jpg")
                .caption("Sabah yogası ile güne başlamak için hiçbir zaman geç değil 🧘‍♀️ #yoga #wellness")
                .moderationStatus(ModerationStatus.APPROVED)
                .likeCount(78)
                .commentCount(0)
                .build());

        Post post3 = postRepository.save(Post.builder()
                .user(athlete1)
                .imageKey("seed/athlete1_post1.jpg")
                .caption("3 ayda 8 kg verdim! Ahmet hocama çok teşekkürler 🔥")
                .moderationStatus(ModerationStatus.APPROVED)
                .likeCount(115)
                .commentCount(0)
                .build());

        Post post4 = postRepository.save(Post.builder()
                .user(athlete2)
                .imageKey("seed/athlete2_post1.jpg")
                .caption("Leg day bitti, hayatım bitti 😂 #legday #squats")
                .moderationStatus(ModerationStatus.APPROVED)
                .likeCount(33)
                .commentCount(0)
                .build());

        // ── Yorumlar ──────────────────────────────────────────────────────────

        postCommentRepository.saveAll(List.of(
                PostComment.builder().post(post1).user(athlete1).content("Hocam süpersiniz, motivasyon aldım!").build(),
                PostComment.builder().post(post1).user(athlete2).content("Hedefim bu 💪").build(),
                PostComment.builder().post(post3).user(trainer1).content("Çok çalıştın, gurur duyuyorum!").build(),
                PostComment.builder().post(post3).user(athlete3).content("Tebrikler kardeşim 🔥").build(),
                PostComment.builder().post(post2).user(athlete1).content("Selin hocam harika içerikler paylaşıyor").build()
        ));

        // commentCount güncelle
        post1.setCommentCount(2);
        post2.setCommentCount(1);
        post3.setCommentCount(2);
        postRepository.saveAll(List.of(post1, post2, post3));

        // ── Takip İlişkileri ─────────────────────────────────────────────────

        followRequestRepository.saveAll(List.of(
                follow(athlete1, trainer1),
                follow(athlete1, trainer2),
                follow(athlete2, trainer1),
                follow(athlete3, trainer1),
                follow(athlete3, trainer2),
                follow(athlete1, athlete2),
                follow(athlete2, athlete3),
                follow(athlete3, athlete1)
        ));

        log.info("DataInitializer: seed verisi başarıyla oluşturuldu.");
    }

    private User createUser(String firstName, String lastName, String username,
                            String email, String password, Gender gender, UserRole role) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .gender(gender)
                .role(role)
                .build();
    }

    private FollowRequest follow(User from, User to) {
        return FollowRequest.builder()
                .fromUser(from)
                .toUser(to)
                .build();
    }
}
