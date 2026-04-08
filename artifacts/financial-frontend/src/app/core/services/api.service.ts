import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { ApiErrorBody } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  handleError(error: HttpErrorResponse) {
    const body = error.error as ApiErrorBody;
    const message = body?.message || error.message || 'Error desconocido';
    const fieldErrors = body?.errors?.map(e => e.message).join(' | ');
    return throwError(() => new Error(fieldErrors || message));
  }
}
