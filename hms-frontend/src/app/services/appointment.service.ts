import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Appointment } from '../models/appointment.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {

  private readonly baseUrl = environment.apiBase + '/appointments';

  constructor(private http: HttpClient) {}

  // ---------------- CREATE ----------------
  create(payload: Appointment): Observable<Appointment> {
    console.log("new Appointment :-", payload);
    return this.http.post<Appointment>(this.baseUrl, payload);
  }

  // ---------------- GET ALL (ADMIN) ----------------
  getAll(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.baseUrl);
  }

  // ---------------- GET BY ID ----------------
  getById(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.baseUrl}/${id}`);
  }

  // ---------------- GET BY DOCTOR ----------------
  getByDoctor(doctorId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.baseUrl}/doctor/${doctorId}`);
  }

  // ---------------- GET BY PATIENT ----------------
  getByPatient(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.baseUrl}/patient/${patientId}`);
  }

  // ---------------- UPDATE STATUS ----------------
  updateStatus(id: number, status: string): Observable<Appointment> {
    return this.http.put<Appointment>(
      `${this.baseUrl}/${id}/status`,
      null,
      { params: { status } }
    );
  }

  // ---------------- CANCEL ----------------
  cancel(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
