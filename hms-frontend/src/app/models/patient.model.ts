// src/app/models/patient.model.ts
export interface Patient {
  id?: number;
  name: string;
  gender: string;
  phone: string;
  address: string;
  medicalHistory?: string;
  disease?: string;
  dob: string;            // yyyy-MM-dd
  admittedDate: string;  // yyyy-MM-dd
  createdAt?: string;
  createdByUsername?: string;
}
