package com.mauabsen.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauabsen.project.models.Attendances;
import com.mauabsen.project.models.Employees;
import com.mauabsen.project.models.EmployeeKycs;
import com.mauabsen.project.repositories.AttendanceRepository;
import com.mauabsen.project.repositories.EmployeeRepository;
import com.mauabsen.project.repositories.EmployeeKycRepository;
import com.mauabsen.project.dto.EmployeeKycDto;
import com.mauabsen.project.exceptions.NotFoundException;
import com.mauabsen.project.dto.AttendanceDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeKycRepository employeeKycRepository;

    public AttendanceDto clockIn(String fingerprintId) {
        // Cari employee berdasarkan fingerprint
        EmployeeKycs kyc = employeeKycRepository.findByFingerprintId(fingerprintId)
            .orElseThrow(() -> new NotFoundException("Invalid fingerprint"));
        
        Employees employee = kyc.getEmployee();

        // Check if already clocked in today
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (attendanceRepository.findByEmployeeIdAndDate(employee.getId().intValue(), today).isPresent()) {
            throw new RuntimeException("Already clocked in today");
        }

        // Create new attendance
        Attendances attendance = Attendances.builder()
            .employee_id(employee.getId().intValue())
            .time_in(LocalDateTime.now())
            .date(today)
            .status("PRESENT")
            .created_at(LocalDateTime.now())
            .build();

        return AttendanceDto.fromEntity(attendanceRepository.save(attendance));
    }

    public AttendanceDto clockOut(String fingerprintId) {
        // Cari employee berdasarkan fingerprint
        EmployeeKycs kyc = employeeKycRepository.findByFingerprintId(fingerprintId)
            .orElseThrow(() -> new NotFoundException("Invalid fingerprint"));
        
        Employees employee = kyc.getEmployee();

        // Get today's attendance
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Attendances attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId().intValue(), today)
            .orElseThrow(() -> new RuntimeException("No clock-in record found"));

        // Update attendance
        attendance.setTime_out(LocalDateTime.now());
        attendance.setUpdated_at(LocalDateTime.now());
        
        return AttendanceDto.fromEntity(attendanceRepository.save(attendance));
    }

    public List<Attendances> getEmployeeAttendances(Integer employeeId) {
        if (!employeeRepository.existsById(employeeId.longValue())) {
            throw new NotFoundException("Employee not found");
        }
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<AttendanceDto> getAttendancesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.findByDateBetween(startDate, endDate)
            .stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    public List<AttendanceDto> getAllAttendances() {
        return attendanceRepository.findAll()
            .stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }
}
