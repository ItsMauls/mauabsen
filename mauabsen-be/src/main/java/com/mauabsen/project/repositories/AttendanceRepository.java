package com.mauabsen.project.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mauabsen.project.models.Attendances;

public interface AttendanceRepository extends JpaRepository<Attendances, Long> {
    
    List<Attendances> findByEmployeeId(Integer employeeId);
    
    @Query("SELECT a FROM Attendances a WHERE a.employee_id = :employeeId AND a.date = :date")
    Optional<Attendances> findByEmployeeIdAndDate(
        @Param("employeeId") Integer employeeId, 
        @Param("date") LocalDateTime date
    );
    
    List<Attendances> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
