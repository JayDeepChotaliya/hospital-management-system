import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { Appointment } from '../../../models/appointment.model';
import { AppointmentService } from '../../../services/appointment.service';
import { AuthService } from '../../../auth/authService';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './appointment-list.component.html',
  styleUrls: ['./appointment-list.component.css']
})
export class AppointmentListComponent {

  appointments$!: Observable<Appointment[]>;

  loading = false;
  error = '';

  isAdmin = false;
  isDoctor = false;
  isPatient = false;

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService
  ) {
    this.setRoles();
    this.loadAppointments();
  }

  // 🔐 Read roles from JWT
  private setRoles(): void {
    const roles = this.authService.getRoles();
    this.isAdmin = roles.includes('ROLE_ADMIN');
    this.isDoctor = roles.includes('ROLE_DOCTOR');
    this.isPatient = roles.includes('ROLE_PATIENT');
  }

  // 📡 Load appointments based on role
  private loadAppointments(): void {
    this.loading = false;
    this.error = '';

    if (this.isAdmin) {
      this.appointments$ = this.appointmentService.getAll();
    }
    else if (this.isDoctor) {
      // TODO: replace hardcoded doctorId with JWT mapping
      const doctorId = 1;
      this.appointments$ = this.appointmentService.getByDoctor(doctorId);
    }
    else if (this.isPatient) {
      // TODO: replace hardcoded patientId with JWT mapping
      const patientId = 1;
      this.appointments$ = this.appointmentService.getByPatient(patientId);
    }
    else {
      this.appointments$ = of([]);
    }

    // loading end handle
    this.appointments$ = this.appointments$.pipe(
      finalize(() => (this.loading = false)),
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load appointments';
        return of([]);
      })
    );
  }

  // ❌ Cancel appointment
  cancel(id: number): void {
    if (!confirm('Cancel this appointment?')) return;

    this.loading = false;

    this.appointmentService.cancel(id).subscribe({
      next: () => this.loadAppointments(),
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to cancel appointment';
      }
    });
  }
}
