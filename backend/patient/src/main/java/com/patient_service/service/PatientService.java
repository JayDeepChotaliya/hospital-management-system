package com.patient_service.service;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.model.Patient;
import com.patient_service.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService
{
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponseDTO addPatient(PatientRequestDTO dto)
    {
        logger.info("Add patient: {}", dto.getName());

        Patient p = new Patient();

        p.setName(dto.getName());
        p.setDob(dto.getDob());
        p.setGender(dto.getGender());
        p.setPhone(dto.getPhone());
        p.setAddress(dto.getAddress());
        p.setMedicalHistory(dto.getMedicalHistory());
        p.setDisease(dto.getDisease());
        p.setAdmittedDate(dto.getAdmittedDate());

        p.setCreatedAt(LocalDateTime.now());
        p.setCreatedByUsername(getCurrentUsername());

        Patient saved = patientRepository.save(p);

        logger.debug("Saved patient id={}", saved.getId());
        return map(saved);
    }

    public List<PatientResponseDTO> getAllPatient()
    {
        logger.info("Get All Patients ");

        return patientRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public PatientResponseDTO getPatientById(Long id)
    {
        logger.info("Get patient id={}", id);
        Patient p = patientRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Patient not found with id " + id));
        return map(p);
    }
    @Transactional
    public PatientResponseDTO updatePatient(Long id ,PatientRequestDTO dto)
    {
        logger.info("Update patient id={}", id);

        Patient p = patientRepository.findById(id)
                                    .orElseThrow(() -> new RuntimeException("Patient not found: " + id));

        p.setName(dto.getName());
        p.setDob(dto.getDob());
        p.setGender(dto.getGender());
        p.setPhone(dto.getPhone());
        p.setAddress(dto.getAddress());
        p.setMedicalHistory(dto.getMedicalHistory());
        p.setDisease(dto.getDisease());
        p.setAdmittedDate(dto.getAdmittedDate());


        Patient saved = patientRepository.save(p);
        logger.debug("Updated patient id={}", saved.getId());
        return map(saved);
    }

    @Transactional
    public void deletePatient(Long id)
    {
        logger.warn("Delete patient id={}", id);
        Patient p = patientRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("Patient not found: " + id));

        patientRepository.delete(p);
    }

    private PatientResponseDTO map(Patient p) {

        PatientResponseDTO responseDto = new PatientResponseDTO();

        responseDto.setId(p.getId());
        responseDto.setName(p.getName());

        LocalDate dob = p.getDob();
        Integer age = null;
        if (dob != null) {
            age = Period.between(dob, LocalDate.now()).getYears();
        }

        responseDto.setAge(age);
        responseDto.setDob(dob);

        responseDto.setGender(p.getGender());
        responseDto.setAddress(p.getAddress());
        responseDto.setDisease(p.getDisease());
        responseDto.setPhone(p.getPhone());
        responseDto.setMedicalHistory(p.getMedicalHistory());
        responseDto.setAdmittedDate(p.getAdmittedDate());

        if(p.getCreatedAt() != null)
        {
            responseDto.setCreatedAt(p.getCreatedAt());
        }
        responseDto.setCreatedByUsername(p.getCreatedByUsername());

        return responseDto;
    }

    private String getCurrentUsername()
    {
        try
        {
            Authentication auth = SecurityContextHolder
                                    .getContext()
                                    .getAuthentication();
            if(auth == null)
                return null;
           return auth.getName();
        }
        catch (Exception ex) {
            logger.debug("Could not get current username from security context: {}", ex.getMessage());
            return null;
        }
    }
}
