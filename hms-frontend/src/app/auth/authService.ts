// src/app/auth/auth.service.ts
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string | null;
  expiresIn?: number;
}

export interface JwtPayload 
{ 
  sub?: string; 
  exp?: number; 
  iat?: number; 
  roles?: string[]; 
  [k: string]: any; 
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'hms_token';
  private readonly baseUrl = environment.authApi || `${environment.apiBase}/auth`;

  private _isAuthenticated$ = new BehaviorSubject<boolean>(false);
  readonly isAuthenticated$ = this._isAuthenticated$.asObservable();

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {
    if (this.isBrowser() && this.hasToken()) {
      this._isAuthenticated$.next(true);
    }
  }


  signup(payload: { username: string; email?: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, payload);
  }

  login(payload: { username: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, payload).pipe(
      tap(res => {
        const token = (res as any)?.token || (res as any)?.accessToken || null;
        if (token) this.setToken(token);
      })
    );
  }

  setToken(token: string | null) {
    if (this.isBrowser()) {
      if (token) {
        localStorage.setItem(this.TOKEN_KEY, token);
        this._isAuthenticated$.next(true);
      } else {
        localStorage.removeItem(this.TOKEN_KEY);
        this._isAuthenticated$.next(false);
      }
    }
  }


  getToken(): string | null {
    if (!this.isBrowser()) return null;
    try {
      console.log("localStorage.getItem - ",localStorage.getItem('hms_token'));
       return localStorage.getItem(this.TOKEN_KEY); 
      } catch { return null; }
  }

  logout() {
    if (this.isBrowser()) { try { localStorage.removeItem(this.TOKEN_KEY); } catch {} }
    this._isAuthenticated$.next(false);
  }

  isAuthenticated(): boolean { return this.hasToken(); }

  getRoles(): string[] {
    const payload = this.decodeToken(this.getToken());
    return Array.isArray(payload?.roles) ? payload.roles : [];
  }


  private decodeToken(token: string | null): any {
    if (!token || !this.isBrowser()) return null;
    try {
      const json = atob(token.split('.')[1]);
      return JSON.parse(json);
    } catch {
      return null;
    }
  }


  private hasToken(): boolean {
    const t = this.getToken();
    if (!t) return false;
    const payload = this.decodeToken(t);
    if (!payload) return true;
    if (payload.exp && typeof payload.exp === 'number') {
      return payload.exp > Math.floor(Date.now()/1000);
    }
    return true;
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
