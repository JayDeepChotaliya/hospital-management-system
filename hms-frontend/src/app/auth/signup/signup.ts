import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../authService';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, 
            ReactiveFormsModule, 
            MatFormFieldModule, 
            MatInputModule, 
            MatButtonModule, 
            MatCardModule,
            MatSelectModule],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class Signup{
    form!: FormGroup;
  
  loading = false;
  error = '';
  roles = [
            { value: 'ROLE_ADMIN', label: 'Admin' },
            { value: 'ROLE_DOCTOR', label: 'Doctor' },
            { value: 'ROLE_PATIENT', label: 'Patient' }
        ];

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) 
  {
      this.form = this.fb.group({ 
                                username: ['', [Validators.required, Validators.minLength(3)]],
                                password: ['', [Validators.required, Validators.minLength(6)]],
                                role: ['', Validators.required] // ADMIN / DOCTOR / PATIENT
                                });
  }
  submit() 
  {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';

    const payload = {
      username: this.form.value.username.trim(),
      password: this.form.value.password,
      role: this.form.value.role
    };

    this.auth.signup(payload).subscribe({
      next: () => {
        this.loading = false;
        // after signup, send to login
        this.router.navigate(['/login']);
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || err?.message || 'Signup failed';
      }
    });
  }
}
