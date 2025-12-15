package com.patient_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class PatientRequestDTO {

    @NotBlank(message = "Name is required ")
    private String name;

    @NotNull(message = "Date of birth is required")
    @Past(message = "DOB must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank(message = "Gender is required ")
    private String gender;

    @Pattern(regexp = "^[0-9]{10}$" , message = "Phone must br 10 digit ")
    private String phone;

    @NotBlank(message = "Address is required ")
    private String address;

    private String medicalHistory;
    private String disease;

    @NotNull(message = "Admitted date is required ")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")

    private LocalDate admittedDate;
}
