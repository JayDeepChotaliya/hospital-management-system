// src/app/models/patient-payload.model.ts
export interface PatientPayload {
  name: string;
  gender: string;
  phone: string;
  address?: string;
  medicalHistory?: string;
  disease?: string;
  dob?: string;            // yyyy-MM-dd
  admittedDate?: string;  // yyyy-MM-dd
}
