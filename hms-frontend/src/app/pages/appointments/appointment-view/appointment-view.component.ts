import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, switchMap } from 'rxjs/operators';
import { Observable, EMPTY } from 'rxjs';

import { AppointmentService } from '../../../services/appointment.service';
import { Appointment } from '../../../models/appointment.model';

@Component({
  selector: 'app-appointment-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appointment-view.component.html',
  styleUrls: ['./appointment-view.component.css']
})
export class AppointmentViewComponent {

  appointment$!: Observable<Appointment>;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService,
    private router: Router
  ) {
    this.appointment$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        return this.appointmentService.getById(id);
      }),
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load appointment';
        return EMPTY;
      })
    );
  }

  goBack(): void {
    this.router.navigate(['/app/appointments']);
  }
}
