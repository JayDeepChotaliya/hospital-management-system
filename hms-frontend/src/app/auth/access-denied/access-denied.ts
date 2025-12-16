import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-access-denied',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  template: `
    <main class="center">
      <mat-card>
        <h2>Access Denied</h2>
        <p>You don't have permission to view this page.</p>
        <div class="actions">
          <button mat-flat-button color="primary" (click)="goHome()">Go to Home</button>
        </div>
      </mat-card>
    </main>
  `,
  styles: [`.center{display:flex;justify-content:center;padding:40px 16px} mat-card{max-width:640px;padding:24px}`]
})
export class AccessDenied {
  constructor(private router: Router) {}
  goHome() { this.router.navigate(['/app']); }
}
