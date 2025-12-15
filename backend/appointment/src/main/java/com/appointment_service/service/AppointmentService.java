package com.appointment_service.service;

import com.appointment_service.dto.AppointmentRequestDTO;
import com.appointment_service.dto.AppointmentResponseDTO;
import com.appointment_service.exception.ConflictException;
import com.appointment_service.exception.NotFoundException;
import com.appointment_service.model.Appointment;
import com.appointment_service.repository.AppointmentRepository;
import com.appointment_service.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository repo;
    private final WebClient webClient;
    private final String doctorServiceUrl;
    private final String patientServiceUrl;
    private final JwtUtil jwtUtil;

    public AppointmentService(AppointmentRepository repo,
                              WebClient.Builder webClientBuilder,
                              @Value("${app.doctor.service.url}")String doctorServiceUrl,
                              @Value("${app.patient.service.url}")String patientServiceUrl,
                              JwtUtil jwtUtil) {
        this.repo = repo;
        this.webClient = webClientBuilder.build();
        this.doctorServiceUrl = doctorServiceUrl;
        this.patientServiceUrl = patientServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create appointment with business checks:
     * - appointmentDate must be in future
     * - doctor and patient must exist (downstream call)
     * - no double booking for doctor or patient at the same timestamp
     */
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto,
                                                    String authorizationHeader)
    {
        logger.info("Create appointment: doctorId={}, patientId={}, at={}",
                        dto.getDoctorId(),
                        dto.getPatientId(),
                        dto.getAppointmentDate());
        // business validation
        LocalDateTime appt = dto.getAppointmentDate();
        if(appt == null || !appt.isAfter(LocalDateTime.now()))
            throw new ConflictException("Appointment time must be in the future", "INVALID_APPOINTMENT_TIME");

        // check downstream services (throws NotFoundException if absent)
        checkDoctorExists(dto.getDoctorId(),authorizationHeader);
        checkPatientExists(dto.getPatientId(), authorizationHeader);

        // prevent double booking (doctor)
        boolean doctorBooked = repo.existsByDoctorIdAndAppointmentDate(dto.getDoctorId(),dto.getAppointmentDate());
        if(doctorBooked)
        {
            logger.warn("Doctor {} already booked at {}", dto.getDoctorId(), dto.getAppointmentDate());
            throw new ConflictException("Doctor already has an appointment at this time", "APPT_CONFLICT");
        }

        // optional: prevent patient double-book (business rule)
        boolean patientBooked = repo.existsByPatientIdAndAppointmentDate(dto.getPatientId(),dto.getAppointmentDate());
        if(patientBooked)
        {
            logger.warn("Patient {} already has appointment at {}", dto.getPatientId(), dto.getAppointmentDate());
            throw new ConflictException("Patient already has an appointment at this time", "APPT_CONFLICT");
        }

        Appointment ap = new Appointment();

        ap.setDoctorId(dto.getDoctorId());
        ap.setPatientId(dto.getPatientId());
        ap.setAppointmentDate(dto.getAppointmentDate());
        ap.setReason(dto.getReason());
        ap.setStatus("SCHEDULED");

        Appointment saved = repo.save(ap);
        logger.debug("Appointment created id={} doctorId={} patientId={}",
                saved.getId(),
                saved.getDoctorId(),
                saved.getPatientId());

        return map(saved);
    }

    public List<AppointmentResponseDTO> getByDoctor(Long doctorId) {
        logger.info("Fetch appointments for doctorId={}", doctorId);
        return repo.findByDoctorId(doctorId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getAll()
    {
        logger.info("Fetch all appointments");
        return repo.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getByPatient(Long patientId) {
        logger.info("Fetch appointments for patientId={}", patientId);
        return repo.findByPatientId(patientId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public AppointmentResponseDTO getById(Long id) {
        logger.info("Fetch appointment id={}", id);
        Appointment a = repo.findById(id)
                            .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        return map(a);
    }

    @Transactional
    public AppointmentResponseDTO updateStatus(Long id, String status) {
        logger.info("Update appointment id={} status={}", id, status);
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        a.setStatus(status);
        Appointment saved = repo.save(a);
        return map(saved);
    }

    @Transactional
    public void cancel(Long id) {
        logger.warn("Cancel appointment id={}", id);
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        a.setStatus("CANCELLED");
        repo.save(a);
        logger.info("Appointment id={} cancelled", id);
    }

    private AppointmentResponseDTO map(Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .doctorId(a.getDoctorId())
                .patientId(a.getPatientId())
                .appointmentDate(a.getAppointmentDate())
                .status(a.getStatus())
                .reason(a.getReason())
                .createdDate(a.getCreatedDate())
                .updatedDate(a.getUpdatedDate())
                .build();
    }

    // OPTIONAL: sample downstream checks (uncomment to use)
    private void checkDoctorExists(Long doctorId, String authHeader) {
        try {
            var resp = webClient.get()
                    .uri(doctorServiceUrl + "/api/v1/doctors/{id}", doctorId)
                    .headers(h -> {
                        if (authHeader != null && !authHeader.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, authHeader);
                        }
                        h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResp -> {
                        // map 404 to NotFound
                        return clientResp.bodyToMono(String.class)
                                .map(body -> new NotFoundException("Doctor not found: " + doctorId, "DOCTOR_NOT_FOUND"));
                    })
                    .toBodilessEntity()
                    .block();

            if (resp == null || resp.getStatusCode().isError()) {
                throw new NotFoundException("Doctor not found: " + doctorId, "DOCTOR_NOT_FOUND");
            }
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error checking doctor existence: {}", ex.getMessage());
            // For safety, treat as not found — or change to different behavior if needed
            throw new NotFoundException("Doctor service unavailable or doctor not found: " + doctorId, "DOCTOR_SERVICE_ERROR");
        }
    }

    private void checkPatientExists(Long patientId, String authHeader) {
        try {
            var resp = webClient.get()
                    .uri(patientServiceUrl + "/api/v1/patients/{id}", patientId)
                    .headers(h -> {
                        if (authHeader != null && !authHeader.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, authHeader);
                        }
                        h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResp -> {
                        return clientResp.bodyToMono(String.class)
                                .map(body -> new NotFoundException("Patient not found: " + patientId, "PATIENT_NOT_FOUND"));
                    })
                    .toBodilessEntity()
                    .block();

            if (resp == null || resp.getStatusCode().isError()) {
                throw new NotFoundException("Patient not found: " + patientId, "PATIENT_NOT_FOUND");
            }
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error checking patient existence: {}", ex.getMessage());
            throw new NotFoundException("Patient service unavailable or patient not found: " + patientId, "PATIENT_SERVICE_ERROR");
        }
    }
}

