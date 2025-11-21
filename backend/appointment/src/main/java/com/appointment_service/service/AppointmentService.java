package com.appointment_service.service;

import com.appointment_service.dto.AppointmentRequestDTO;
import com.appointment_service.dto.AppointmentResponseDTO;
import com.appointment_service.model.Appointment;
import com.appointment_service.repository.AppointmentRepository;
import com.appointment_service.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentRepository repo;
    private final WebClient.Builder webClientBuilder;
    private final String doctorServiceUrl;
    private final String patientServiceUrl;
    private final JwtUtil jwtUtil;

    public AppointmentService(AppointmentRepository repo,
                              WebClient.Builder webClientBuilder,
                              @Value("${app.doctor.service.url}")String doctorServiceUrl,
                              @Value("${app.patient.service.url}")String patientServiceUrl,
                              JwtUtil jwtUtil) {
        this.repo = repo;
        this.webClientBuilder = webClientBuilder;
        this.doctorServiceUrl = doctorServiceUrl;
        this.patientServiceUrl = patientServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto, String header)
    {
        logger.info("Create appointment: doctorId={}, patientId={}, at={}",
                        dto.getDoctorId(),
                        dto.getPatientId(),
                        dto.getAppointmentDate());
        Appointment ap = new Appointment();

        ap.setDoctorId(dto.getDoctorId());
        ap.setPatientId(dto.getPatientId());
        ap.setAppointmentDate(dto.getAppointmentDate());
        ap.setReason(dto.getReason());
        ap.setStatus("SCHEDULED");
        Appointment saved = repo.save(ap);
        logger.debug("Appointment saved id={}", saved.getId());

        return map(saved);
    }

    public List<AppointmentResponseDTO> getByDoctor(Long doctorId) {
        logger.info("Fetch appointments for doctorId={}", doctorId);
        return repo.findByDoctorId(doctorId).stream().map(this::map).collect(Collectors.toList());
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
        return repo.findByPatientId(patientId).stream().map(this::map).collect(Collectors.toList());
    }

    public AppointmentResponseDTO getById(Long id) {
        logger.info("Fetch appointment id={}", id);
        Appointment a = repo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        return map(a);
    }

    public AppointmentResponseDTO updateStatus(Long id, String status) {
        logger.info("Update appointment id={} status={}", id, status);
        Appointment a = repo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        a.setStatus(status);
        Appointment saved = repo.save(a);
        return map(saved);
    }

    public void cancel(Long id) {
        logger.warn("Cancel appointment id={}", id);
        Appointment a = repo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
        a.setStatus("CANCELLED");
        repo.save(a);
    }

    private AppointmentResponseDTO map(Appointment a) {
        return new AppointmentResponseDTO(a.getId(),
                                            a.getDoctorId(),
                                            a.getPatientId(),
                                            a.getAppointmentDate(),
                                            a.getStatus(),
                                            a.getReason(),
                                            a.getCreatedDate(),
                                            a.getUpdatedDate());
    }

    // OPTIONAL: sample downstream checks (uncomment to use)
    private void checkDoctorExists(Long doctorId, String authHeader) {
        // example: GET /doctors/{id} from doctor service
        var resp = webClientBuilder.build()
                .get()
                .uri(doctorServiceUrl + "/doctors/{id}", doctorId)
                .headers(h ->
                {
                    if(authHeader != null) h.set("Authorization", authHeader);
                })
                .retrieve()
                .toBodilessEntity()
                .block();
        if (resp == null || resp.getStatusCode().isError()) {
            throw new RuntimeException("Doctor not found: " + doctorId);
        }
    }

    private void checkPatientExists(Long patientId, String authHeader) {
        var resp = webClientBuilder.build()
                .get()
                .uri(patientServiceUrl + "/patients/{id}", patientId)
                .headers(h -> { if(authHeader != null) h.set("Authorization", authHeader); })
                .retrieve()
                .toBodilessEntity()
                .block();
        if (resp == null || resp.getStatusCode().isError()) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
    }
}
