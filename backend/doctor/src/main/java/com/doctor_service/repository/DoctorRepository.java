package com.doctor_service.repository;

import com.doctor_service.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor,Long>
{

}
