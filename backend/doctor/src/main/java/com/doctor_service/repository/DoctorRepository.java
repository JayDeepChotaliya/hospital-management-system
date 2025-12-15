package com.doctor_service.repository;

import com.doctor_service.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long>
{

    boolean existsByEmail(String email);

    Optional<Doctor> findByEmail(String email);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByDepartment(String department);
}
