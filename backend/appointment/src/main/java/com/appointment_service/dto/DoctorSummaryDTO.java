package com.appointment_service.dto;

import lombok.Data;

@Data
public class DoctorSummaryDTO{

    private Long id;
    private String name;
    private String email;
    private String specialization;
}
