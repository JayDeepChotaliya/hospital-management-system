package com.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponseDTO {

    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    private String medicalHistory;
    private String disease;
    private LocalDate admittedDate;
    private LocalDate dob;
    private LocalDateTime createdAt;
    private String createdByUsername;

}
