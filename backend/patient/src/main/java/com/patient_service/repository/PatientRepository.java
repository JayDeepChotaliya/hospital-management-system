package com.patient_service.repository;

import com.patient_service.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>
{

  //  Optional<Patient> findByUserUsername(String username);
}
