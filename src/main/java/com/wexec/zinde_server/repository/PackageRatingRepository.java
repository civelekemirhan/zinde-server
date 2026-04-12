package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PackageRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PackageRatingRepository extends JpaRepository<PackageRating, Long> {

    boolean existsByUserIdAndTrainerPackageId(UUID userId, Long packageId);

    Optional<PackageRating> findByUserIdAndTrainerPackageId(UUID userId, Long packageId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM PackageRating r WHERE r.trainerPackage.id = :packageId")
    double avgRatingByPackageId(@Param("packageId") Long packageId);

    @Query("SELECT COUNT(r) FROM PackageRating r WHERE r.trainerPackage.id = :packageId")
    long countByPackageId(@Param("packageId") Long packageId);
}
