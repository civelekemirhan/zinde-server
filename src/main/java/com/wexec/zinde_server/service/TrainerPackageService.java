package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.CreateTrainerPackageRequest;
import com.wexec.zinde_server.dto.response.TrainerPackageResponse;
import com.wexec.zinde_server.entity.TrainerPackage;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.entity.UserRole;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.TrainerPackageRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerPackageService {

    private final TrainerPackageRepository packageRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrainerPackageResponse create(UUID trainerId, CreateTrainerPackageRequest req) {
        User trainer = getUser(trainerId);
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("NOT_TRAINER", "Sadece antrenörler paket oluşturabilir.", HttpStatus.FORBIDDEN);
        }

        TrainerPackage pkg = packageRepository.save(TrainerPackage.builder()
                .trainer(trainer)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .durationDays(req.getDurationDays())
                .totalLessons(req.getTotalLessons())
                .build());

        return toResponse(pkg);
    }

    public List<TrainerPackageResponse> getByTrainer(UUID trainerId) {
        return packageRepository.findByTrainerIdAndActiveOrderByCreatedAtDesc(trainerId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TrainerPackageResponse toggleActive(UUID trainerId, Long packageId) {
        TrainerPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("PACKAGE_NOT_FOUND", "Paket bulunamadı.", HttpStatus.NOT_FOUND));

        if (!pkg.getTrainer().getId().equals(trainerId)) {
            throw new AppException("FORBIDDEN", "Bu paketi düzenleme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        pkg.setActive(!pkg.isActive());
        return toResponse(packageRepository.save(pkg));
    }

    private TrainerPackageResponse toResponse(TrainerPackage pkg) {
        return TrainerPackageResponse.builder()
                .id(pkg.getId())
                .trainerId(pkg.getTrainer().getId())
                .trainerUsername(pkg.getTrainer().getUsername())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .durationDays(pkg.getDurationDays())
                .totalLessons(pkg.getTotalLessons())
                .active(pkg.isActive())
                .createdAt(pkg.getCreatedAt())
                .build();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
