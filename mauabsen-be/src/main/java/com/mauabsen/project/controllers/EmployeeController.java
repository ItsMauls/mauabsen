package com.mauabsen.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.mauabsen.project.dto.EmployeeDto;
import com.mauabsen.project.dto.EmployeeKycRequestDto;
import com.mauabsen.project.services.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

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
}
