import { Injectable } from '@angular/core';
import {
            CanActivate,
            CanActivateChild,
            ActivatedRouteSnapshot,
            Router,
            UrlTree
        } from '@angular/router';
import { AuthService } from './authService';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate, CanActivateChild {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    return this.checkRoles(route);
  }

  canActivateChild(route: ActivatedRouteSnapshot): boolean | UrlTree {
    return this.checkRoles(route);
  }

  private checkRoles(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const requiredRoles = route.data?.['roles'] as string[] | undefined;


    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    const userRoles = this.auth.getRoles();

    const allowed = requiredRoles.some(role =>
      userRoles.includes(role)
    );

    return allowed
      ? true
      : this.router.createUrlTree(['/access-denied']);
  }
}
