import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { take } from 'rxjs/operators';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

import { PatientService } from '../../../services/patient.service';
import { PatientPayload } from '../../../models/patient.payload';

@Component({
  selector: 'app-patient-add',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './patient-add.component.html',
  styleUrls: ['./patient-add.component.css']
})
export class PatientAddComponent {

  loading = false;
  error = '';

  form = this.fb.group({
    name: ['', Validators.required],
    dob: [null as Date | null, Validators.required],
    gender: ['', Validators.required],
    phone: ['', Validators.required],
    address: ['', Validators.required],
    medicalHistory: [''],
    disease: [''],
    admittedDate: [null as Date | null, Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    const payload: PatientPayload = {
      name: this.form.value.name!.trim(),
      gender: this.form.value.gender!,
      phone: this.form.value.phone!.trim(),
      address: this.form.value.address!.trim(),
      medicalHistory: this.form.value.medicalHistory || undefined,
      disease: this.form.value.disease || undefined,
      dob: this.formatDate(this.form.value.dob!),
      admittedDate: this.formatDate(this.form.value.admittedDate!)
    };

    this.patientService.create(payload).pipe(take(1)).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/app/patients']);
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to create patient';
      }
    });
  }

  private formatDate(value: string | Date | null | undefined): string | undefined {
      if (!value) return undefined;

      // If already yyyy-MM-dd (from <input type="date">)
      if (typeof value === 'string') {
        return value;
      }

      // If Date object (future proof / Material datepicker)
      if (value instanceof Date) {
        return value.toISOString().slice(0, 10);
      }

      return undefined;
  }

  cancel(): void {
    this.router.navigate(['/app/patients']);
  }
}

