import { Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap, take } from 'rxjs/operators';


import { PatientService } from '../../../services/patient.service';
import { PatientPayload } from '../../../models/patient.payload';

@Component({
  selector: 'app-patient-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './patient-edit.component.html',
  styleUrls: ['./patient-edit.component.css']
})
export class PatientEditComponent {

  loading = false;
  error = '';
  private patientId!: number;

  form = this.fb.group({
    name: ['', Validators.required],
    dob: [null as string | null],
    gender: ['', Validators.required],
    phone: ['', Validators.required],
    address: [''],
    medicalHistory: [''],
    disease: [''],
    admittedDate: [null as string | null]
  });

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private patientService: PatientService,
    private router: Router
    ) {
    this.loadPatient();
  }

  private loadPatient(): void {
    this.loading = false;

    this.route.paramMap
      .pipe(
        take(1),
        switchMap(params => {
          const id = params.get('id');
          if (!id) throw new Error('Invalid patient id');
          this.patientId = +id;
          return this.patientService.getById(this.patientId);
        })
      )
      .subscribe({
        next: patient => {
          const dob = new Date(patient.dob);
          this.form.patchValue({
            name: patient.name,
            gender: patient.gender,
            phone: patient.phone,
            address: patient.address,
            medicalHistory: patient.medicalHistory,
            disease: patient.disease,
            dob: patient.dob ,
            admittedDate: patient.admittedDate
          });
          this.loading = false;
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to load patient';
        }
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = false;
    this.error = '';

    const dob = this.form.value.dob;
    console.info("submit => ",dob);
    const admitted = this.form.value.admittedDate;

    const payload: PatientPayload = {
      name: (this.form.value.name ?? '').trim(),
      gender: this.form.value.gender ?? '',
      phone: (this.form.value.phone ?? '').trim(),
      address: this.form.value.address?.trim(),
      medicalHistory: this.form.value.medicalHistory?.trim(),
      disease: this.form.value.disease?.trim(),
      dob: dob ? new Date(dob).toISOString().slice(0, 10) : undefined,
      admittedDate: admitted ? new Date(admitted).toISOString().slice(0, 10) : undefined
    };

    this.patientService.update(this.patientId, payload)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/patients']);
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to update patient';
        }
      });
  }

  private toDateInput(value: string | Date | null | undefined): string | null {
    if (!value) return null;

    // already yyyy-MM-dd
    if (typeof value === 'string') {
      return value;
    }

    // Date object → yyyy-MM-dd
    if (value instanceof Date) {
      return value.toISOString().slice(0, 10);
    }

  return null;
}


  cancel(): void {
    this.router.navigate(['/app/patients']);
  }
}
