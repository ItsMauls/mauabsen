package com.mauabsen.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.mauabsen.project.models.Attendances;
import com.mauabsen.project.services.AttendanceService;
import com.mauabsen.project.dto.AttendanceRequestDto;
import com.mauabsen.project.dto.AttendanceResponseDto;
import com.mauabsen.project.dto.AttendanceDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody Map<String, String> request) {
        try {
            String fingerprintTemplate = request.get("fingerprintTemplate");
            int matchScore = Integer.parseInt(request.get("matchScore"));
            
            if (matchScore < 100) {
                Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Fingerprint does not match. Score: " + matchScore
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            AttendanceDto attendance = attendanceService.clockIn(fingerprintTemplate, matchScore);
            
            String employeeName = attendance.getEmployeeName();
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Welcome " + employeeName + "! Clock in successful",
                "matchScore", matchScore,
                "attendance", attendance
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody Map<String, String> request) {
        try {
            String fingerprintTemplate = request.get("fingerprintTemplate");
            int matchScore = Integer.parseInt(request.get("matchScore"));
            
            if (matchScore < 100) {
                Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Fingerprint does not match. Score: " + matchScore
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            AttendanceDto attendance = attendanceService.clockOut(fingerprintTemplate, matchScore);
            
            String employeeName = attendance.getEmployeeName();
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Goodbye " + employeeName + "! Clock out successful",
                "matchScore", matchScore,
                "attendance", attendance
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
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
