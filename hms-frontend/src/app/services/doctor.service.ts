import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Doctor } from "../models/doctor.model";
import { Observable } from "rxjs";

@Injectable({providedIn:'root'})
export class DoctorService{

    private readonly baseUrl = `${environment.apiBase}/doctors`;

    constructor(private http : HttpClient) {}

    create(payload: Doctor): Observable<Doctor>
    {
        return this.http.post<Doctor>(this.baseUrl,payload);
    }

    getAll(): Observable<Doctor[]>
    {
        console.log("Service baseUrl :- ",this.baseUrl);
        return this.http.get<Doctor[]>(this.baseUrl);
    }

    getById(id:number): Observable<Doctor>
    {
        return this.http.get<Doctor>(`${this.baseUrl}/${id}`);
    }

    // 🔹 UPDATE
    update(id: number, payload: Doctor): Observable<Doctor> {
        return this.http.put<Doctor>(`${this.baseUrl}/${id}`, payload);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}