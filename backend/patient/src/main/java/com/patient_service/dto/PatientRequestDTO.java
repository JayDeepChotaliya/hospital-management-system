package com.patient_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDTO {

    private String name;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String medicalHistory;
    private String disease;
    private LocalDate admittedDate;
}
