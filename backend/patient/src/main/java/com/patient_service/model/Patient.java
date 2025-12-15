package com.patient_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(length = 10)
    private String gender;
    @Column(columnDefinition = "TEXT")
    private String address;
    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    private LocalDate dob;
    @Column(length = 20)
    private String phone;
    @Column(length = 200)
    private String disease;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDate admittedDate;

    @Column(nullable = false)
    private String createdByUsername;

}
