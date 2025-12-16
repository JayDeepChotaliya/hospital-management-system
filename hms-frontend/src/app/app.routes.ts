import { Routes } from '@angular/router';
import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';

export const routes: Routes = [

  /* ================= PUBLIC ROUTES ================= */

  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login').then(m => m.Login)
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./auth/signup/signup').then(m => m.Signup)
  },
  {
    path: 'access-denied',
    loadComponent: () =>
      import('./auth/access-denied/access-denied').then(m => m.AccessDenied)
  },

  /* ================= PROTECTED APP ================= */

  {
    path: 'app',
    loadComponent: () =>
      import('./layout/shell/shell').then(m => m.Shell),
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [

      /* -------- Dashboard -------- */
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/dashboard').then(m => m.Dashboard)
      },

      /* ================= PATIENT ================= */

      {
        path: 'patients',
        loadComponent: () =>
          import('./pages/patients/patient-list/patient-list.component')
            .then(m => m.PatientListComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
      },
      {
        path: 'patients/add',
        loadComponent: () =>
          import('./pages/patients/patient-add/patient-add.component')
            .then(m => m.PatientAddComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'patients/view/:id',
        loadComponent: () =>
          import('./pages/patients/patient-view/patient-view.component')
            .then(m => m.PatientViewComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
      },
      {
        path: 'patients/edit/:id',
        loadComponent: () =>
          import('./pages/patients/patient-edit/patient-edit.component')
            .then(m => m.PatientEditComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },

      /* ================= DOCTOR ================= */

      {
        path: 'doctors',
        loadComponent: () =>
          import('./pages/doctors/doctor-list/doctor-list.component')
            .then(m => m.DoctorListComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'doctors/add',
        loadComponent: () =>
          import('./pages/doctors/doctor-add/doctor-add.component')
            .then(m => m.DoctorAddComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'doctors/view/:id',
        loadComponent: () =>
          import('./pages/doctors/doctor-view/doctor-view.component')
            .then(m => m.DoctorViewComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'doctors/edit/:id',
        loadComponent: () =>
          import('./pages/doctors/doctor-edit/doctor-edit.component')
            .then(m => m.DoctorEditComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      /* ===================== APPOINTMENTS ===================== */

      // LIST (ADMIN)
      {
        path: 'appointments',
        loadComponent: () =>
          import('./pages/appointments/appointment-list/appointment-list.component')
            .then(m => m.AppointmentListComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN','ROLE_DOCTOR','ROLE_PATIENT'] }
      },

      // CREATE (PATIENT, ADMIN)
      {
        path: 'appointments/create',
        loadComponent: () =>
          import('./pages/appointments/appointment-create/appointment-create.component')
            .then(m => m.AppointmentCreateComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_PATIENT', 'ROLE_ADMIN'] }
      },

      // VIEW (ADMIN, DOCTOR, PATIENT)
      {
        path: 'appointments/view/:id',
        loadComponent: () =>
          import('./pages/appointments/appointment-view/appointment-view.component')
            .then(m => m.AppointmentViewComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'] }
      },

      // UPDATE STATUS (DOCTOR, ADMIN)
      {
        path: 'appointments/edit/:id',
        loadComponent: () =>
          import('./pages/appointments/appointment-status/appointment-status.component')
            .then(m => m.AppointmentStatusComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_DOCTOR', 'ROLE_ADMIN'] }
      },

      // CANCEL (PATIENT, ADMIN)
      {
        path: 'appointments/:id/cancel',
        loadComponent: () =>
          import('./pages/appointments/appointment-cancel/appointment-cancel.component')
            .then(m => m.AppointmentCancelComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ROLE_PATIENT', 'ROLE_ADMIN'] }
      }
      /* ========================================================= */
    ]
  },
  
  /* ================= FALLBACK ================= */

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
