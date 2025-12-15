package com.appointment_service.controller;

import com.appointment_service.dto.AppointmentRequestDTO;
import com.appointment_service.dto.AppointmentResponseDTO;
import com.appointment_service.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController
{
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping(consumes = "application/json" , produces = "application/json")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> create(@Valid
                                                            @RequestBody AppointmentRequestDTO dto,
                                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION,
                                                                    required = false) String authHeader)
    {
        logger.info("[CREATE] Request to create appointment: doctorId={}, patientId={}, by={}",
                dto.getDoctorId(), dto.getPatientId(), currentUsername());

        // Optionally: verify that the authenticated patient is creating only their own appointment.
        // TODO: Implement mapping from currentUsername() -> patientId and throw AccessDeniedException if mismatch.
        // Example:
        // if (hasRole("PATIENT") && !currentUserOwnsPatient(currentUsername(), dto.getPatientId())) {
        //     throw new AccessDeniedException("Patients can only create appointments for themselves");
        // }

        AppointmentResponseDTO saved = service.createAppointment(dto, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all (ADMIN)
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        logger.info("[GET ALL] by={}", currentUsername());
        return ResponseEntity.ok(service.getAll());
    }

    // Get by id (doctor/patient/admin)
    @GetMapping(value = "/{id}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable Long id) {
        logger.info("[GET] id={} by={}", id, currentUsername());

        // TODO: Optional owner check:
        // - If PATIENT role: ensure the appointment.patientId == current user's patient id
        // - If DOCTOR role: ensure the appointment.doctorId == current user's doctor id
        // Implement currentUserOwnsAppointment(...) to perform this check if you have mapping available.

        return ResponseEntity.ok(service.getById(id));
    }

    // By doctor
    @GetMapping(value = "/doctor/{doctorId}" , produces = "application/json")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        logger.info("[GET BY DOCTOR] doctorId={} by={}", doctorId, currentUsername());
        // TODO: If you want doctors to only fetch their own appointments, compare current user -> doctorId

        return ResponseEntity.ok(service.getByDoctor(doctorId));
    }

    // By patient
    @GetMapping(value = "/patient/{patientId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable Long patientId) {
        logger.info("[GET BY PATIENT] patientId={} by={}", patientId, currentUsername());

        // TODO: Enforce that patients can only fetch their own appointments:
        // if (hasRole("PATIENT") && !currentUserOwnsPatient(currentUsername(), patientId)) { throw new AccessDeniedException(...); }

        return ResponseEntity.ok(service.getByPatient(patientId));
    }

    // Update status (doctor/admin)
    @PutMapping(value = "/{id}/status", produces = "application/json")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(@PathVariable Long id,
                                                               @RequestParam String status) {
        logger.info("[UPDATE STATUS] id={} status={} by={}", id, status, currentUsername());

        // Optionally verify doctor owns appointment before allowing status change (if doctor role)
        // TODO: implement check if needed
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // Cancel (patient/admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        logger.info("[CANCEL] id={} requested by={}", id, currentUsername());

        // TODO: If patient role, ensure patient owns the appointment
        service.cancel(id);
        return ResponseEntity.ok("Appointment cancelled id=" + id);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : String.valueOf(auth.getName());
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        Collection<? extends GrantedAuthority> auths = auth.getAuthorities();
        if (auths == null)
            return false;
        String wanted = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return auths
                .stream().
                map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase(wanted));
    }

    // Example placeholders for ownership checks (implement mapping logic)
    // private boolean currentUserOwnsPatient(String username, Long patientId) { ... }
    // private boolean currentUserOwnsDoctor(String username, Long doctorId) { ... }
    // private boolean currentUserOwnsAppointment(String username, Long appointmentId) { ... }
}
