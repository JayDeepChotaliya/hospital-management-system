import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormGroup, FormControl } from '@angular/forms'
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../authService';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';



@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
            CommonModule,
            RouterModule,
            ReactiveFormsModule,
            MatFormFieldModule,
            MatInputModule,
            MatButtonModule,
            MatCardModule,
            MatProgressSpinnerModule
          ],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  form!: FormGroup;      // declared, will be initialized in constructor
  loading = false;
  error = '';
  

  constructor(private fb: FormBuilder, 
              private auth: AuthService, 
              private router: Router) {
    // initialize form using object syntax (NOT array). This avoids TS/template issues.
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }
  
  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    this.auth.login(this.form.value).subscribe({
      next: res => {
        this.loading = false;
        const token = (res as any)?.token || (res as any)?.accessToken || this.auth.getToken();
        if (token) {
          const redirect = sessionStorage.getItem('redirect_after_login');
          if (redirect) { sessionStorage.removeItem('redirect_after_login'); this.router.navigateByUrl(redirect); }
          else this.router.navigate(['/app']);
        } else {
          this.error = 'Login failed: token missing';
        }
      },
      error: err => { this.loading = false; this.error = err?.error?.message || err?.message || 'Login failed'; }
    });
   
  }
  gotoSignup() { this.router.navigate(['/signup']); }
}
