package com.mauabsen.project.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee_kycs")
public class EmployeeKycs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer employee_id;
    @Column(length = 1000)
    private String address;
    private String first_name;
    private String last_name;
    @Column(columnDefinition = "TEXT")
    private String fingerprint_id;
    @Column(length = 1000)
    private String photo_url;
    private String status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToOne
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employees employee;

    @Column(columnDefinition = "TEXT")
    private String physical_fingerprint;
    
    @Column(columnDefinition = "TEXT")
    private String device_fingerprint;
}

