import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { ApiService } from './api.service';
import {
  Customer, CustomerRequest, ApiResponse, HalCustomers
} from '../models/customer.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly base = `${environment.apiUrl}/api/v1/customers`;

  constructor(private http: HttpClient, private api: ApiService) {}

  getAll(): Observable<Customer[]> {
    return this.http.get<HalCustomers>(`${this.base}?size=200&sort=id,desc`).pipe(
      map(res => res._embedded?.customers ?? []),
      catchError(err => this.api.handleError(err))
    );
  }

  getById(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.base}/${id}`).pipe(
      catchError(err => this.api.handleError(err))
    );
  }

  create(data: CustomerRequest): Observable<ApiResponse<Customer>> {
    return this.http.post<ApiResponse<Customer>>(this.base, data).pipe(
      catchError(err => this.api.handleError(err))
    );
  }

  update(id: number, data: CustomerRequest): Observable<ApiResponse<Customer>> {
    return this.http.put<ApiResponse<Customer>>(`${this.base}/${id}`, data).pipe(
      catchError(err => this.api.handleError(err))
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      catchError(err => this.api.handleError(err))
    );
  }
}
