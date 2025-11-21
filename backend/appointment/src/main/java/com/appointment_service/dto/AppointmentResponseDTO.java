package com.appointment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private LocalDateTime appointmentTime;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
