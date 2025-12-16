import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { take } from 'rxjs/operators';

import { AppointmentService } from '../../../services/appointment.service';
import { AuthService } from '../../../auth/authService';

@Component({
  selector: 'app-appointment-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './appointment-create.component.html',
  styleUrls: ['./appointment-create.component.css']
})
export class AppointmentCreateComponent {

  loading = false;
  error = '';

  form = this.fb.group({
    doctorId: [null, [Validators.required]],
    patientId: [null, [Validators.required]],
    appointmentDate: ['', Validators.required], // datetime-local
    reason: ['']
  });

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private auth: AuthService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = false;
    this.error = '';

    const rawDate = this.form.value.appointmentDate!;

    // 🔥 HTML datetime-local → backend LocalDateTime
    const appointmentDate = new Date(rawDate).toISOString().slice(0, 19);

    const payload = {
      doctorId: Number(this.form.value.doctorId),
      patientId: Number(this.form.value.patientId),
      appointmentDate,
      reason: this.form.value.reason?.trim()
    };

    this.appointmentService.create(payload)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/appointments']);
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to create appointment';
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/app/appointments']);
  }
}
