package com.mauabsen.project.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Employees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String phone_number;
    private String password;
    private String role;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private EmployeeKycs employeeKycs;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Attendances> attendances;
}

