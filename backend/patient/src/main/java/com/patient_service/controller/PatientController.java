package com.patient_service.controller;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.model.Patient;
import com.patient_service.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;
    public PatientController(PatientService patientService)
    {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> create(@Valid @RequestBody PatientRequestDTO dto)
    {
        log.info("Create patient request: {}",dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.addPatient(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<List<PatientResponseDTO>> all()
    {
        log.info("Get all patients");
        return ResponseEntity.ok(patientService.getAllPatient());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> one(@PathVariable Long id)
    {
        log.info("Get patient id={}", id);
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> update(@PathVariable Long id,
                                                     @RequestBody PatientRequestDTO dto)
    {
        log.info("Update patient id={}", id);
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        log.warn("Delete patient id={}", id);
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
