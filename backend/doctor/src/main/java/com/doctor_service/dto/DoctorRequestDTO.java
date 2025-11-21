package com.doctor_service.dto;

import lombok.Data;

@Data
public class DoctorRequestDTO {

    private String name;
    private String email;
    private String specialization;
    private String phone;
    private int experience;
    private String qualification;
    private String department;
}
