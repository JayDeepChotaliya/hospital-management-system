package com.doctor_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorRequestDTO {

    @NotBlank(message = "Doctor name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @Min(value = 0, message = "Experience must be 0 or a positive number")
    @Max(value = 60, message = "Experience seems too large")
    private int experience;

    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;
}
