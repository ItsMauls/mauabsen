package com.mauabsen.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mauabsen.project.dto.EmployeeDto;
import com.mauabsen.project.dto.EmployeeKycRequestDto;
import com.mauabsen.project.models.EmployeeKycs;
import com.mauabsen.project.models.Employees;
import com.mauabsen.project.services.EmployeeService;
import com.mauabsen.project.services.AttendanceService;
import com.mauabsen.project.dto.AttendanceDto;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto employeeDto) {
        // Validasi basic di controller
        if (employeeDto.getUsername() == null || employeeDto.getEmail() == null) {
            throw new IllegalArgumentException("Username and email are required");
        }
        
        // Default role jika tidak diset
        if (employeeDto.getRole() == null) {
            employeeDto.setRole("USER");
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.createEmployee(employeeDto));
    }

    @PostMapping("/{id}/kyc")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDto> registerEmployeeKyc(
            @PathVariable Long id,
            @RequestBody EmployeeKycRequestDto kycRequest) {
        return ResponseEntity.ok(employeeService.registerEmployeeKyc(id, kycRequest));
    }

    @PostMapping("/{id}/fingerprint")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDto> registerFingerprint(
            @PathVariable Long id,
            @RequestParam String fingerprintId) {
        return ResponseEntity.ok(employeeService.registerFingerprint(id, fingerprintId));
    }

    @GetMapping("/current/fingerprint")
    public ResponseEntity<Map<String, String>> getCurrentEmployeeFingerprint() {
        Employees employee = getCurrentEmployee();
        EmployeeKycs kyc = employee.getEmployeeKycs();
        
        if (kyc == null || kyc.getFingerprint_id() == null) {
            throw new RuntimeException("No fingerprint registered");
        }

        // Decode the stored hash back to the original template
        String fingerprintTemplate = kyc.getFingerprint_id();
        try {
            // If the template is Base64 encoded, decode it first
            byte[] decodedBytes = Base64.getDecoder().decode(fingerprintTemplate);
            fingerprintTemplate = Base64.getEncoder().encodeToString(decodedBytes);
        } catch (IllegalArgumentException e) {
            // If it's not Base64 encoded, use it as is
        }

        Map<String, String> response = new HashMap<>();
        response.put("fingerprintTemplate", fingerprintTemplate);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-fingerprint")
    public ResponseEntity<Map<String, String>> verifyFingerprint(@RequestBody Map<String, Object> request) {
        String fingerprintTemplate = (String) request.get("fingerprintTemplate");
        
        // Use AttendanceService to handle clock in which includes fingerprint verification
        AttendanceDto attendanceResult = attendanceService.clockIn(fingerprintTemplate);
        
        // If we get here, verification was successful
        Employees employee = getCurrentEmployee();
        
        // Create response with welcome message
        Map<String, String> response = new HashMap<>();
        String welcomeMessage = "Welcome " + employee.getEmployeeKycs().getFirst_name();
        response.put("message", welcomeMessage);
        response.put("matchScore", String.valueOf(attendanceResult.getMatchScore()));
        
        return ResponseEntity.ok(response);
    }

    private Employees getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeService.findByUsername(username);
    }
}
