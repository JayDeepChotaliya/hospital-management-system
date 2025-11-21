package com.appointment_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {

    @NotNull
    private Long doctorId;

    @NotNull
    private Long patientId;

    @NotNull
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentDate;

    private String reason;
}
