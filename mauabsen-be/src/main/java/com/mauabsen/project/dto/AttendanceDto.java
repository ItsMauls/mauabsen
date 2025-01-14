package com.mauabsen.project.dto;

import java.time.LocalDateTime;
import com.mauabsen.project.models.Attendances;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Integer employeeId;
    private String employeeName;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private LocalDateTime date;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer matchScore;

    public static AttendanceDto fromEntity(Attendances attendance) {
        return AttendanceDto.builder()
            .id(attendance.getId())
            .employeeId(attendance.getEmployee_id())
            .timeIn(attendance.getTime_in())
            .timeOut(attendance.getTime_out())
            .date(attendance.getDate())
            .status(attendance.getStatus())
            .createdAt(attendance.getCreated_at())
            .updatedAt(attendance.getUpdated_at())
            .build();
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
