import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../../auth/authService';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule,
    MatSidenavModule, 
    MatToolbarModule, 
    MatIconModule, 
    MatListModule,
    MatButtonModule],
  templateUrl: './shell.html',
  styleUrls: ['./shell.css']
})
export class Shell
{
  isSmall$ = this.bo.observe('(max-width:768px)').pipe(map(r => r.matches));
  menu = [
    { label: 'Dashboard', path: '/app/dashboard', roles: [], icon: '' },
    { label: 'Patients', path: '/app/patients', roles: ['ROLE_ADMIN','ROLE_DOCTOR'], icon: '' },
    { label: 'Doctors', path: '/app/doctors', roles: ['ROLE_ADMIN'], icon: '' },
    { label: 'Appointments', path: '/app/appointments', roles: ['ROLE_ADMIN','ROLE_DOCTOR','ROLE_PATIENT'], icon: '' }
  ];
  filteredMenu = this.menu;

  constructor(private bo: BreakpointObserver, private auth: AuthService)
  {
    const roles = this.auth.getRoles();
    console.log("Roles   ",roles);
    this.filteredMenu = this.menu.filter(i => !i.roles || i.roles.length === 0 || i.roles.some(r => roles.includes(r)));
    console.log("this.filteredMenu ",this.filteredMenu);
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
  closeIfSmall(drawer: any) {
    this.isSmall$.pipe(take(1)).subscribe(isSmall => {
      if (isSmall) drawer.close();
    });
  }

  

}
