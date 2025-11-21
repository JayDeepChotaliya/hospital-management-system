package com.doctor_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String specialization;
    private String phone;
    private int experience;
    private String qualification;
    private String department;
}
