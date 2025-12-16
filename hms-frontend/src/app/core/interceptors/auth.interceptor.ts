import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../auth/authService';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  console.log('🔥 Interceptor fired, token =', token);

  const isAuthUrl =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/signup');

  if (token && !isAuthUrl) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
