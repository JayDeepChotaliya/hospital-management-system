import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import {
  CanActivate,
  CanActivateChild,
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree
} from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from './authService';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(
    private auth: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {

    // 🚨 SSR guard
    if (!isPlatformBrowser(this.platformId)) {
      return true;
    }

    if (this.auth.isAuthenticated()) {
      return true;
    }

    // Browser only
    try {
      sessionStorage.setItem('redirect_after_login', state.url);
    } catch {}

    return this.router.createUrlTree(['/login']);
  }

  canActivateChild(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    return this.canActivate(route, state);
  }
}
