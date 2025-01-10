package com.mauabsen.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauabsen.project.dto.AuthRequestDto;
import com.mauabsen.project.dto.AuthResponseDto;
import com.mauabsen.project.services.AuthService;
import com.mauabsen.project.services.EmployeeService;
import com.mauabsen.project.dto.EmployeeDto;
import com.mauabsen.project.repositories.EmployeeRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/init-admin")
    public ResponseEntity<EmployeeDto> initAdmin(@RequestBody AuthRequestDto request) {
        if (employeeRepository.existsByRole("ROLE_ADMIN")) {
            throw new RuntimeException("Admin already exists");
        }

        EmployeeDto adminDto = EmployeeDto.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .email("admin@example.com")
            .role("ROLE_ADMIN")
            .build();

        return ResponseEntity.ok(employeeService.createEmployee(adminDto));
    }
} 