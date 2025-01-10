package com.mauabsen.project.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mauabsen.project.models.Employees;

public interface EmployeeRepository extends JpaRepository<Employees, Long> {
    Optional<Employees> findByUsername(String username);
    boolean existsByRole(String role);
    Optional<Employees> findByEmail(String email);
}
