package com.doctor_service.service;

import com.doctor_service.dto.DoctorRequestDTO;
import com.doctor_service.dto.DoctorResponseDTO;
import com.doctor_service.exception.ApiException;
import com.doctor_service.model.Doctor;
import com.doctor_service.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final Logger logger = LoggerFactory.getLogger(DoctorService.class);
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public DoctorResponseDTO addDoctor(DoctorRequestDTO dto)
    {
        logger.info("Add doctor: {}", dto.getEmail());

        if(doctorRepository.existsByEmail(dto.getEmail()))
        {
            logger.warn("Attempt to create doctor with existing email: {}", dto.getEmail());
            throw new ApiException("Doctor with this email already exists", "DOCTOR_EMAIL_EXISTS");
        }

        Doctor  doctor = new Doctor();
        doctor.setName(dto.getName());
        doctor.setEmail(dto.getEmail());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setPhone(dto.getPhone());
        doctor.setExperience(dto.getExperience());
        doctor.setQualification(dto.getQualification());
        doctor.setDepartment(dto.getDepartment());

        Doctor saved = doctorRepository.save(doctor);
        logger.debug("Saved doctor id={}", saved.getId());
        return map(saved);
    }

    public List<DoctorResponseDTO> getAllDoctor()
    {
        logger.info("Get all doctors");
        return doctorRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public DoctorResponseDTO getDoctorById(Long id)
    {
        logger.info("Get doctor id={}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(()-> new ApiException("Doctor not found by id : "+id , "DOCTOR_NOT-FOUND"));
        return map(doctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Long id , DoctorRequestDTO dto)
    {
        logger.info("Update doctor id={} email={}", id, dto.getEmail());

        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(()-> new ApiException("Doctor not found by id : "+id ,"DOCTOR_NOT_FOUND"));

        // If email is changed, ensure it's not used by another doctor
        if(!existing.getEmail().equalsIgnoreCase(dto.getEmail()) && doctorRepository.existsByEmail(dto.getEmail()))
        {
            logger.warn("Attempt to update doctor id={} to an email that already exists: {}", id, dto.getEmail());
            throw new ApiException("Another doctor with this email already exists","DOCTOR_EMAIL_EXISTS");
        }

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setSpecialization(dto.getSpecialization());
        existing.setPhone(dto.getPhone());
        existing.setExperience(dto.getExperience());
        existing.setQualification(dto.getQualification());
        existing.setDepartment(dto.getDepartment());

        Doctor saved = doctorRepository.save(existing);
        logger.debug("Updated doctor id={}", saved.getId());
        return map(saved);
    }

    @Transactional
    public void deleteDoctor(Long id)
    {
        logger.warn("Delete doctor id={}", id);
        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(()-> new ApiException("Doctor not found by id : "+id,"DOCTOR_NOT_FOUND"));
        doctorRepository.delete(existing);
    }

    private DoctorResponseDTO map(Doctor d) {
        return new DoctorResponseDTO(d.getId(),
                                        d.getName(),
                                        d.getEmail(),
                                        d.getSpecialization(),
                                        d.getPhone(),
                                        d.getExperience(),
                                        d.getQualification(),
                                        d.getDepartment()
                                    );
    }
}
