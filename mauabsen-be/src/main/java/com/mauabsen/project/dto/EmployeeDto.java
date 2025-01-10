package com.mauabsen.project.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.mauabsen.project.models.Employees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private EmployeeKycDto employeeKyc;
    private List<AttendanceDto> attendances;

    public static EmployeeDto fromEntity(Employees employee) {
        return EmployeeDto.builder()
            .id(employee.getId())
            .username(employee.getUsername())
            .email(employee.getEmail())
            .phoneNumber(employee.getPhone_number())
            .role(employee.getRole())
            .createdAt(employee.getCreated_at())
            .updatedAt(employee.getUpdated_at())
            .employeeKyc(employee.getEmployeeKycs() != null ?
                EmployeeKycDto.fromEntity(employee.getEmployeeKycs()) : null)
            .attendances(employee.getAttendances() != null ? employee.getAttendances().stream().map(AttendanceDto::fromEntity).collect(Collectors.toList()) : null)
            .build();
    }

    public Employees toEntity() {
        return Employees.builder()
            .id(this.id)
            .username(this.username)
            .email(this.email)
            .phone_number(this.phoneNumber)
            .role(this.role)
            .created_at(this.createdAt)
            .updated_at(this.updatedAt)
            .build();
    }
}
