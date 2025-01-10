package com.mauabsen.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mauabsen.project.dto.EmployeeDto;
import com.mauabsen.project.repositories.EmployeeRepository;
import com.mauabsen.project.exceptions.NotFoundException;
import com.mauabsen.project.models.Employees;
import com.mauabsen.project.dto.EmployeeKycRequestDto;
import com.mauabsen.project.models.EmployeeKycs;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public EmployeeDto findByUsername(String username) {
        return employeeRepository.findByUsername(username)
        .map(EmployeeDto::fromEntity)
        .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        // Validasi business logic
        validateNewEmployee(employeeDto);
        
        // Mapping ke entity
        Employees employee = employeeDto.toEntity();
        
        // Set additional fields
        employee.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        employee.setCreated_at(LocalDateTime.now());
        employee.setUpdated_at(null);
        
        // Save ke database
        try {
            Employees savedEmployee = employeeRepository.save(employee);
            return EmployeeDto.fromEntity(savedEmployee);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Failed to create employee. Possible duplicate username or email.");
        }
    }

    private void validateNewEmployee(EmployeeDto employeeDto) {
        // Check duplicate username
        if (employeeRepository.findByUsername(employeeDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Validate email format
        if (!isValidEmail(employeeDto.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }
        
        // Validate password strength
        if (employeeDto.getPassword() == null || employeeDto.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public EmployeeDto registerEmployeeKyc(Long employeeId, EmployeeKycRequestDto request) {
        Employees employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new NotFoundException("Employee not found"));

        EmployeeKycs kyc = EmployeeKycs.builder()
            .employee_id(employeeId.intValue())
            .address(request.getAddress())
            .first_name(request.getFirstName())
            .last_name(request.getLastName())
            .fingerprint_id(request.getFingerprintId())
            .photo_url(request.getPhotoUrl())
            .status("ACTIVE")
            .created_at(LocalDateTime.now())
            .build();

        employee.setEmployeeKycs(kyc);
        Employees updatedEmployee = employeeRepository.save(employee);
        
        return EmployeeDto.fromEntity(updatedEmployee);
    }

    public EmployeeDto registerFingerprint(Long employeeId, String fingerprintId) {
        Employees employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new NotFoundException("Employee not found"));

        EmployeeKycs kyc = employee.getEmployeeKycs();
        if (kyc == null) {
            kyc = new EmployeeKycs();
            kyc.setEmployee_id(employeeId.intValue());
            kyc.setCreated_at(LocalDateTime.now());
        }

        kyc.setFingerprint_id(fingerprintId);
        kyc.setUpdated_at(LocalDateTime.now());

        employee.setEmployeeKycs(kyc);
        Employees updatedEmployee = employeeRepository.save(employee);

        return EmployeeDto.fromEntity(updatedEmployee);
    }

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }
}
