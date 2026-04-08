import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { ApiService } from './api.service';
import { Transaction, TransactionRequest, HalTransactions } from '../models/transaction.model';
import { ApiResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly base = '/api/v1/transactions';

  constructor(private http: HttpClient, private api: ApiService) {}

  getLast5(accountId: number): Observable<Transaction[]> {
    return this.http
      .get<HalTransactions>(`${this.base}/search/byAccount?accountId=${accountId}&page=0&size=5`)
      .pipe(
        map(res => res._embedded?.transactions ?? []),
        catchError(err => this.api.handleError(err))
      );
  }

  register(accountId: number, data: TransactionRequest): Observable<ApiResponse<Transaction>> {
    return this.http
      .post<ApiResponse<Transaction>>(`${this.base}/account/${accountId}`, data)
      .pipe(
        catchError(err => this.api.handleError(err))
      );
  }
}
