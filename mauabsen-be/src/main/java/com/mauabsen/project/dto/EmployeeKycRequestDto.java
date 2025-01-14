package com.mauabsen.project.dto;

import lombok.Data;

@Data
public class EmployeeKycRequestDto {
    private Long employeeId;
    private String address;
    private String firstName;
    private String lastName;
    private String fingerprintId;  // Ubah nama field sesuai dengan yang digunakan
    private String photoUrl;
} 