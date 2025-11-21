package com.patient_service.controller;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.model.Patient;
import com.patient_service.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {


    private final PatientService patientService;

    public PatientController(PatientService patientService) {this.patientService = patientService;}

    @PostMapping
    public ResponseEntity<PatientResponseDTO> create(@RequestBody PatientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.addPatient(dto));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> all()
    {
        return ResponseEntity.ok(patientService.getAllPatient());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> one(@PathVariable Long id)
    {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> update(@PathVariable Long id,
                                                     @RequestBody PatientRequestDTO dto)
    {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id)
    {
        patientService.deletePatient(id);
        return ResponseEntity.ok("Deleted id=" + id);
    }

}
