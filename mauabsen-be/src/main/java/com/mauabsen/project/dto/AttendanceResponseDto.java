package com.mauabsen.project.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import com.mauabsen.project.models.Attendances;

@Data
@Builder
public class AttendanceResponseDto {
    private Long id;
    private Integer employeeId;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private String status;
    private LocalDateTime date;

    public static AttendanceResponseDto fromEntity(Attendances attendance) {
        return AttendanceResponseDto.builder()
            .id(attendance.getId())
            .employeeId(attendance.getEmployee_id())
            .timeIn(attendance.getTime_in())
            .timeOut(attendance.getTime_out())
            .status(attendance.getStatus())
            .date(attendance.getDate())
            .build();
    }
}