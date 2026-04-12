package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {

    boolean existsByUserIdAndTrainerPackageId(UUID userId, Long packageId);

    Optional<PackagePurchase> findByUserIdAndTrainerPackageId(UUID userId, Long packageId);

    List<PackagePurchase> findByUserIdOrderByPurchasedAtDesc(UUID userId);
}
