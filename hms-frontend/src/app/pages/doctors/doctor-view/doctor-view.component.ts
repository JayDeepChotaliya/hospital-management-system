import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';

import { Doctor } from '../../../models/doctor.model';
import { DoctorService } from '../../../services/doctor.service';

@Component({
  selector: 'app-doctor-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './doctor-view.component.html',
  styleUrls: ['./doctor-view.component.css']
})
export class DoctorViewComponent {

  doctor$!: Observable<Doctor>;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private doctorService: DoctorService,
    private router: Router
  ) {
    // 🔥 route param → backend → observable
    this.doctor$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        return this.doctorService.getById(id);
      }),
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load doctor details';
        return EMPTY;
      })
    );
  }

  goBack(): void {
    this.router.navigate(['/app/doctors']);
  }
}
