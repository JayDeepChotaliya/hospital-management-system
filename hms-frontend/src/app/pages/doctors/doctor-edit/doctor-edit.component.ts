import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap, tap, catchError } from 'rxjs/operators';
import { Observable, EMPTY } from 'rxjs';

import { Doctor } from '../../../models/doctor.model';
import { DoctorService } from '../../../services/doctor.service';

@Component({
  selector: 'app-doctor-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './doctor-edit.component.html',
  styleUrls: ['./doctor-edit.component.css']
})
export class DoctorEditComponent {

  doctor$!: Observable<Doctor>;
  loading = false;
  error = '';

  private doctorId!: number;

  form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    specialization: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    experience: [0, [Validators.required, Validators.min(0), Validators.max(60)]],
    qualification: [''],
    department: ['', Validators.required]
  });

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private router: Router
  ) {
    // 🔥 Load doctor → patch form
    this.doctor$ = this.route.paramMap.pipe(
      switchMap(params => {
        this.doctorId = Number(params.get('id'));
        return this.doctorService.getById(this.doctorId);
      }),
      tap(doctor => {
        this.form.patchValue({
          name: doctor.name,
          email: doctor.email,
          specialization: doctor.specialization,
          phone: doctor.phone,
          experience: doctor.experience,
          qualification: doctor.qualification,
          department: doctor.department
        });
      }),
      catchError(err => {
        this.error = err?.error?.message || 'Failed to load doctor';
        return EMPTY;
      })
    );
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = false;
    this.error = '';

    const payload: Doctor = {
      name: this.form.value.name!.trim(),
      email: this.form.value.email!.trim(),
      specialization: this.form.value.specialization!,
      phone: this.form.value.phone!.trim(),
      experience: this.form.value.experience!,
      qualification: this.form.value.qualification?.trim(),
      department: this.form.value.department!
    };

    this.doctorService.update(this.doctorId, payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/app/doctors']);
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to update doctor';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/app/doctors']);
  }
}

