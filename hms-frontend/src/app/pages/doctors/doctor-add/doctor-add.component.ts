import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { take } from 'rxjs/operators';

import { DoctorService } from '../../../services/doctor.service';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-doctor-add',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './doctor-add.component.html',
  styleUrls: ['./doctor-add.component.css']
})
export class DoctorAddComponent {

  loading = false;
  error = '';

  form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    specialization: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    experience: [0, [Validators.required, Validators.min(0), Validators.max(60)]],
    qualification: [''],
    department: ['', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private router: Router
  ) {}

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

    this.doctorService.create(payload)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/doctors']);
        },
        error: err => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to create doctor';
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/app/doctors']);
  }
}
