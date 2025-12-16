// src/app/models/appointment.model.ts

export interface Appointment {
  id?: number;

  doctorId: number;
  patientId: number;

  // Backend: LocalDateTime → string (ISO)
  appointmentDate: string;

  status?: string;      // CREATED / CONFIRMED / CANCELLED / COMPLETED
  reason?: string;

  createdDate?: string;
  updatedDate?: string;
}
