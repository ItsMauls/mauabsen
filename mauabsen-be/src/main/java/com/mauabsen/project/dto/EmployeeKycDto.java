package com.mauabsen.project.dto;

import com.mauabsen.project.models.EmployeeKycs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeKycDto {
    private String address;
    private String firstName;
    private String lastName;    
    private String fingerprintId;
    private String photoUrl;
    private String status;

    public static EmployeeKycDto fromEntity(EmployeeKycs employeeKyc) {
        return EmployeeKycDto.builder()
            .address(employeeKyc.getAddress())
            .firstName(employeeKyc.getFirst_name())
            .lastName(employeeKyc.getLast_name())            
            .fingerprintId(employeeKyc.getFingerprint_id())
            .photoUrl(employeeKyc.getPhoto_url())
            .status(employeeKyc.getStatus())
            .build();
    }

    public EmployeeKycs toEntity(EmployeeKycDto dto) {
        return EmployeeKycs.builder()
            .address(dto.getAddress())
            .first_name(dto.getFirstName())
            .last_name(dto.getLastName())
            .fingerprint_id(dto.getFingerprintId())
            .photo_url(dto.getPhotoUrl())
            .status(dto.getStatus())
            .build();
    }
}
