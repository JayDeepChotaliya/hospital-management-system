import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { take } from 'rxjs/operators';

import { AppointmentService } from '../../../services/appointment.service';

@Component({
  selector: 'app-appointment-status',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './appointment-status.component.html',
  styleUrls: ['./appointment-status.component.css']
})
export class AppointmentStatusComponent {

  loading = false;
  error = '';
 
  appointmentId!: number;

  statuses = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

  form = this.fb.group({
    status: ['', Validators.required]
  });

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private router: Router
  ) {
    this.appointmentId = Number(this.route.snapshot.paramMap.get('id'));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = false;
    this.error = '';

    const status = this.form.value.status!;

    this.appointmentService
      .updateStatus(this.appointmentId, status)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/appointments']);
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to update status';
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/app/appointments']);
  }
}
