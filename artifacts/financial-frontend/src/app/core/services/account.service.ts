import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { ApiService } from './api.service';
import { Account, AccountRequest, HalAccounts } from '../models/account.model';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly base = '/api/v1/accounts';

  constructor(private http: HttpClient, private api: ApiService) {}

  getByCustomer(customerId: number): Observable<Account[]> {
    return this.http
      .get<HalAccounts>(`${this.base}/search/byCustomer?customerId=${customerId}`)
      .pipe(
        map(res => res._embedded?.accounts ?? []),
        catchError(err => this.api.handleError(err))
      );
  }

  create(data: AccountRequest): Observable<ApiResponse<Account>> {
    return this.http.post<ApiResponse<Account>>(this.base, data).pipe(
      catchError(err => this.api.handleError(err))
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      catchError(err => this.api.handleError(err))
    );
  }
}
