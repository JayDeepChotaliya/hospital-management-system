package com.doctor_service.controller;

import com.doctor_service.dto.DoctorRequestDTO;
import com.doctor_service.dto.DoctorResponseDTO;
import com.doctor_service.service.DoctorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ---------------- CREATE ----------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid  @RequestBody DoctorRequestDTO dto) {
        logger.info("[CREATE] Request received to add new doctor: name={}, specialization={}",
                dto.getName(), dto.getSpecialization());

        DoctorResponseDTO created = doctorService.addDoctor(dto);

        logger.info("[CREATE] Doctor created successfully with id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------------- READ ALL ----------------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<DoctorResponseDTO>> all() {
        logger.info("[GET ALL] Fetching all doctors");
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctor();
        logger.info("[GET ALL] Total doctors found={}", doctors.size());
        return ResponseEntity.ok(doctors);
    }

    // ---------------- READ BY ID ----------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        logger.info("[GET] Fetching doctor by id={}", id);
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        logger.info("[GET] Doctor found: {}", doctor.getName());
        return ResponseEntity.ok(doctor);
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id,
                                                          @RequestBody DoctorRequestDTO dto) {
        logger.info("[UPDATE] Updating doctor with id={} email={}", id,dto.getEmail());
        DoctorResponseDTO updated = doctorService.updateDoctor(id, dto);
        logger.info("[UPDATE] Doctor updated successfully id={} newSpecialization={}",
                id, updated.getSpecialization());
        return ResponseEntity.ok(updated);
    }

    // ---------------- DELETE ----------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id) {
        logger.warn("[DELETE] Request to delete doctor with id={}", id);
        doctorService.deleteDoctor(id);
        logger.info("DELETE] Doctor deleted successfully with id={}", id);
        return ResponseEntity.ok("Doctor deleted successfully with id " + id);
    }

}
