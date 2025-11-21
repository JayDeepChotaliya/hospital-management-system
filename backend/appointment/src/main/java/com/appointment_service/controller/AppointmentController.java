package com.appointment_service.controller;

import com.appointment_service.dto.AppointmentRequestDTO;
import com.appointment_service.dto.AppointmentResponseDTO;
import com.appointment_service.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController
{
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT') or hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> create(@Validated
                                                            @RequestBody AppointmentRequestDTO dto,
                                                            @RequestHeader(value = "Authorization", required = false) String authHeader)
    {
        logger.info("Request to create appointment by header user");
        AppointmentResponseDTO saved = service.createAppointment(dto, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all (ADMIN)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get by id (doctor/patient/admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('DOCTOR') or hasAuthority('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // By doctor
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('DOCTOR') or hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(service.getByDoctor(doctorId));
    }

    // By patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('PATIENT') or hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getByPatient(patientId));
    }

    // Update status (doctor/admin)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DOCTOR') or hasAuthority('ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // Cancel (patient/admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT') or hasAuthority('ADMIN')")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.ok("Appointment cancelled id=" + id);
    }
}
