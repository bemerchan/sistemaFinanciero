import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { ApiService } from './api.service';
import { Transaction, TransactionRequest, HalTransactions } from '../models/transaction.model';
import { ApiResponse } from '../models/customer.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly base = `${environment.apiUrl}/api/v1/transactions`;

  constructor(private http: HttpClient, private api: ApiService) {}

  getByAccount(accountId: number, page = 0, size = 10): Observable<HalTransactions> {
    return this.http
      .get<HalTransactions>(
        `${this.base}/search/byAccount?accountId=${accountId}&page=${page}&size=${size}&sort=id,desc`
      )
      .pipe(catchError(err => this.api.handleError(err)));
  }

  register(accountId: number, data: TransactionRequest): Observable<ApiResponse<Transaction>> {
    return this.http
      .post<ApiResponse<Transaction>>(`${this.base}/account/${accountId}`, data)
      .pipe(catchError(err => this.api.handleError(err)));
  }
}
