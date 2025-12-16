// src/app/services/patient.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Patient } from '../models/patient.model';
import { PatientPayload } from '../models/patient.payload';

@Injectable({ providedIn: 'root' })
export class PatientService {

  private readonly baseUrl = `${environment.apiBase}/patients`;

  constructor(private http: HttpClient) {}

  // CREATE
  create(payload: PatientPayload): Observable<Patient> {
    console.info("payload :- ",payload);
    return this.http.post<Patient>(this.baseUrl, payload);
  }

  // GET ALL
  getAll(): Observable<Patient[]> {
    console.info("baseUrl", this.baseUrl);
    return this.http.get<Patient[]>(this.baseUrl);
  }

  // GET BY ID
  getById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.baseUrl}/${id}`);
  }

  // UPDATE
  update(id: number, payload: PatientPayload): Observable<Patient> {
    return this.http.put<Patient>(`${this.baseUrl}/${id}`, payload);
  }

  // DELETE
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
