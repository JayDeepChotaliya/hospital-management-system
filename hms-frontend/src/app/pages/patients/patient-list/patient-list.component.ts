import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { PatientService } from '../../../services/patient.service';
import { Patient } from '../../../models/patient.model';
import { AuthService } from '../../../auth/authService';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {

  patients$!: Observable<Patient[]>;
  loading = false;
  error: string | null = null;

  isAdmin = false;
  isDoctor = false;

  constructor(
    private patientService: PatientService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.setRoles();
    this.loadPatients();
  }

  private setRoles(): void {
    const roles = this.authService.getRoles();
    this.isAdmin = roles.includes('ROLE_ADMIN');
    this.isDoctor = roles.includes('ROLE_DOCTOR');
  }

  private loadPatients(): void {
    this.loading = false;
    this.error = null;


    this.patients$ = this.patientService.getAll();
    /*.pipe(
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load patients';
        return of([]);
      }),
      finalize(() => {
        this.loading = false;
      })
    );*/

    console.info("patients ",this.patients$.forEach);
  }

  deletePatient(id: number): void {
    if (!confirm('Are you sure you want to delete this patient?')) return;

    this.loading = false;

    this.patientService.delete(id).subscribe({
      next: () => {
        this.router.navigate(['/app/patients']);
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to delete patient';
      }
    });
  }
}
