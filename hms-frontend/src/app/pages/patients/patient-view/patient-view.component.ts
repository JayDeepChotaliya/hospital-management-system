import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { Patient } from '../../../models/patient.model';
import { PatientService } from '../../../services/patient.service';

@Component({
  selector: 'app-patient-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './patient-view.component.html',
  styleUrls: ['./patient-view.component.css']
})
export class PatientViewComponent implements OnInit {

  patient$!: Observable<Patient | null>;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private patientService: PatientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPatient();
  }

  private loadPatient(): void {
    this.patient$ = this.route.paramMap.pipe(
      map(params => Number(params.get('id'))),
      switchMap(id => {
        if (!id) {
          this.error = 'Invalid patient id';
          return of(null);
        }
        return this.patientService.getById(id);
      }),
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load patient';
        return of(null);
      })
    );
  }

  goBack(): void {
    this.router.navigate(['/app/patients']);
  }
}
