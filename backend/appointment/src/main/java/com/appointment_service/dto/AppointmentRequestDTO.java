package com.appointment_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDTO {

    @NotNull(message = "doctorId is required")
    @Positive(message = "doctorId must be a positive number")
    private Long doctorId;

    @NotNull(message = "patientId is required")
    @Positive(message = "patientId must be a positive number")
    private Long patientId;

    @NotNull(message = "appointmentDate is required")
    @Future(message = "Appointment time must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentDate;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
