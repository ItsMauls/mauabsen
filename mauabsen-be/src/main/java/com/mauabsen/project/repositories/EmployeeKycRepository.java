package com.mauabsen.project.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.mauabsen.project.models.EmployeeKycs;

public interface EmployeeKycRepository extends JpaRepository<EmployeeKycs, Long> {
    @Query("SELECT e FROM EmployeeKycs e WHERE e.fingerprint_id = :fingerprintId")
    Optional<EmployeeKycs> findByFingerprintId(@Param("fingerprintId") String fingerprintId);
}
