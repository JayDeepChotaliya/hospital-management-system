package com.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponseDTO {

    private Long id;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String medicalHistory;
    private String disease;
    private LocalDate admittedDate;
}
