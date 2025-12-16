import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { Doctor } from '../../../models/doctor.model';
import { DoctorService } from '../../../services/doctor.service';
import { AuthService } from '../../../auth/authService';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.css']
})
export class DoctorListComponent {

  doctors$!: Observable<Doctor[]>;
  loading = false;
  error = '';

  isAdmin = false;

  constructor(
    private doctorService: DoctorService,
    private authService: AuthService
  ) {
    this.setRoles();
    this.loadDoctors();
  }

  private setRoles(): void {
    const roles = this.authService.getRoles();
    this.isAdmin = roles.includes('ROLE_ADMIN');
  }

  private loadDoctors(): void {
    this.loading = false;
    this.error = '';

    this.doctors$ = this.doctorService.getAll().pipe(
      tap(() => this.loading = false),
      catchError(err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to load doctors';
        return of([]); // async pipe safe
      })
    );
  }

  deleteDoctor(id: number | undefined): void {
    if (!id) return;

    if (!confirm('Are you sure you want to delete this doctor?')) {
      return;
    }

    this.doctorService.delete(id).subscribe({
      next: () => this.loadDoctors(),
      error: err => {
        this.error = err?.error?.message || 'Failed to delete doctor';
      }
    });
  }
}
