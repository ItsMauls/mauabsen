package com.mauabsen.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mauabsen.project.dto.AuthRequestDto;
import com.mauabsen.project.dto.AuthResponseDto;
import com.mauabsen.project.exceptions.UnauthorizedException;
import com.mauabsen.project.models.Employees;
import com.mauabsen.project.repositories.EmployeeRepository;
import com.mauabsen.project.security.JwtTokenProvider;

import java.util.List;
import java.util.Optional;


@Service
public class AuthService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponseDto login(AuthRequestDto request) {
        System.out.println("Login request received: " + request);
        
        List<Employees> allEmployees = employeeRepository.findAll();
        System.out.println("All employees in database:");
        allEmployees.forEach(emp -> {
            System.out.println("ID: " + emp.getId() + 
                             ", Username: " + emp.getUsername() + 
                             ", Email: " + emp.getEmail() + 
                             ", Role: " + emp.getRole());
        });
        
        System.out.println("Searching for username: '" + request.getUsername() + "'");
        
        Optional<Employees> employeeOpt = employeeRepository.findByUsername(request.getUsername());
        
        System.out.println("Found employee: " + employeeOpt.isPresent());
        if(employeeOpt.isPresent()) {
            System.out.println("Employee details: " + employeeOpt.get());
        } else {
            System.out.println("No employee found with username: " + request.getUsername());
        }
        
        Employees employee = employeeOpt.get();
            
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String role = employee.getRole().startsWith("ROLE_") ? 
            employee.getRole() : "ROLE_" + employee.getRole();

        String token = jwtTokenProvider.createToken(employee.getUsername(), role);
        
        return AuthResponseDto.builder()
            .token(token)
            .username(employee.getUsername())
            .role(role)
            .build();
    }
} 

