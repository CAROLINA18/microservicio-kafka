import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken'); // Obtener el token del almacenamiento local
    console.log(token)
    return new HttpHeaders({
      Authorization: `Bearer ${token}`, // Agregar el token al encabezado
    });
  }

  createUser(payload: any): Observable<any> {
    const headers = this.getHeaders(); // Obtener encabezados con el token
    return this.http.post('http://localhost:8084/api/create', payload, { headers });
  }

  updateUser(payload: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.put('http://localhost:8085/api/update', payload, { headers });
  }

  deleteUser(userId: number): Observable<any> {
    const headers = this.getHeaders();
    return this.http.delete(`http://localhost:8088/usuarios/${userId}`, { headers });
  }

  getUsers(): Observable<any[]> {
    const token = localStorage.getItem('authToken'); // Obtener el token desde localStorage
    console.log(token)
    const headers = {
      Authorization: `Bearer ${token}`, // Agregar el encabezado Authorization
    };
    return this.http.get<any[]>(`${this.apiUrl}/users`, { headers });
  }

  getUser(userId: number): Observable<any> {
    const headers = this.getHeaders();
    return this.http.get<any>(`${this.apiUrl}/users/${userId}`, { headers });
  }
}
