package com.mauabsen.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mauabsen.project.models.Attendances;
import com.mauabsen.project.services.AttendanceService;
import com.mauabsen.project.dto.AttendanceRequestDto;
import com.mauabsen.project.dto.AttendanceResponseDto;
import com.mauabsen.project.dto.AttendanceDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDto> clockIn(@RequestParam String fingerprintId) {
        return ResponseEntity.ok(attendanceService.clockIn(fingerprintId));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDto> clockOut(@RequestParam String fingerprintId) {
        return ResponseEntity.ok(attendanceService.clockOut(fingerprintId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Attendances>> getEmployeeAttendances(
            @PathVariable Integer employeeId) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendances(employeeId));
    }

    @GetMapping("/range")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceDto>> getAttendancesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(attendanceService.getAttendancesByDateRange(startDate, endDate));
    }

    @GetMapping
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceDto>> getAllAttendances() {
        return ResponseEntity.ok(attendanceService.getAllAttendances());
    }
}
