import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { take } from 'rxjs/operators';

import { AppointmentService } from '../../../services/appointment.service';

@Component({
  selector: 'app-appointment-cancel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appointment-cancel.component.html',
  styleUrls: ['./appointment-cancel.component.css']
})
export class AppointmentCancelComponent {

  appointmentId!: number;
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService,
    private router: Router
  ) {
    this.appointmentId = Number(this.route.snapshot.paramMap.get('id'));
  }

  confirmCancel(): void {
    if (!confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    this.loading = false;
    this.error = '';

    this.appointmentService.cancel(this.appointmentId)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/appointments']);
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to cancel appointment';
        }
      });
  }

  back(): void {
    this.router.navigate(['/app/appointments', this.appointmentId]);
  }
}
