import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Account } from '../../../core/models/account.model';
import { TransactionService } from '../../../core/services/transaction.service';

export interface TransactionDialogData {
  account: Account;
}

@Component({
  selector: 'app-transaction-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule, MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Nueva transacción</h2>

    <mat-dialog-content class="tx-dialog-content">

      <!-- Account summary -->
      <div class="tx-account-summary">
        <div class="tx-account-icon" [class]="account.accountType === 'SAVINGS' ? 'savings' : 'checking'">
          <mat-icon>{{ account.accountType === 'SAVINGS' ? 'savings' : 'account_balance_wallet' }}</mat-icon>
        </div>
        <div class="tx-account-info">
          <div class="tx-account-number">{{ account.accountNumber }}</div>
          <div class="tx-account-type">Cuenta de {{ account.accountType === 'SAVINGS' ? 'Ahorro' : 'Corriente' }}</div>
        </div>
        <div class="tx-account-balance">
          <div class="tx-balance-label">Saldo actual</div>
          <div class="tx-balance-amount" [style.color]="account.balance < 0 ? '#ef4444' : '#1e293b'">
            {{ formatCurrency(account.balance) }}
          </div>
        </div>
      </div>

      <!-- Form -->
      <form [formGroup]="form" class="tx-dialog-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Tipo de transacción *</mat-label>
          <mat-select formControlName="transactionType">
            <mat-select-trigger>
              <div style="display:flex;align-items:center;gap:8px">
                <mat-icon [style.color]="form.get('transactionType')?.value === 'DEPOSIT' ? '#16a34a' : '#ef4444'"
                          style="font-size:18px;width:18px;height:18px;line-height:18px">
                  {{ form.get('transactionType')?.value === 'DEPOSIT' ? 'south_west' : 'north_east' }}
                </mat-icon>
                {{ form.get('transactionType')?.value === 'DEPOSIT' ? 'Consignación' : 'Retiro' }}
              </div>
            </mat-select-trigger>
            <mat-option value="DEPOSIT">
              <div style="display:flex;align-items:center;gap:8px">
                <mat-icon style="color:#16a34a;font-size:18px;width:18px;height:18px">south_west</mat-icon>
                Consignación
              </div>
            </mat-option>
            <mat-option value="WITHDRAWAL">
              <div style="display:flex;align-items:center;gap:8px">
                <mat-icon style="color:#ef4444;font-size:18px;width:18px;height:18px">north_east</mat-icon>
                Retiro
              </div>
            </mat-option>
          </mat-select>
          <mat-error>{{ getError('transactionType') }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Monto *</mat-label>
          <span matTextPrefix>$ &nbsp;</span>
          <input matInput type="number" step="0.01" min="0.01"
                 formControlName="amount" placeholder="0.00">
          <mat-error>{{ getError('amount') }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Descripción (opcional)</mat-label>
          <input matInput formControlName="description" placeholder="Ej: Pago de nómina">
          <mat-error>{{ getError('description') }}</mat-error>
        </mat-form-field>
      </form>

    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" [disabled]="loading">Cancelar</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="loading">
        @if (loading) {
          <mat-spinner diameter="16" style="display:inline-block;margin-right:8px"></mat-spinner>
        }
        {{ loading ? 'Procesando...' : 'Registrar transacción' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .tx-dialog-content {
      padding-top: 8px !important;
      overflow: visible !important;
      min-width: 420px;
    }
    .tx-account-summary {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 16px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      margin-bottom: 20px;
    }
    .tx-account-icon {
      width: 40px; height: 40px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      &.savings  { background: #dbeafe; mat-icon { color: #3b82f6; } }
      &.checking { background: #dcfce7; mat-icon { color: #16a34a; } }
      mat-icon { font-size: 20px; width: 20px; height: 20px; }
    }
    .tx-account-info { flex: 1; }
    .tx-account-number { font-family: monospace; font-weight: 700; font-size: 14px; color: #0f172a; }
    .tx-account-type   { font-size: 12px; color: #64748b; margin-top: 2px; }
    .tx-account-balance { text-align: right; flex-shrink: 0; }
    .tx-balance-label  { font-size: 11px; color: #94a3b8; font-weight: 500; }
    .tx-balance-amount { font-size: 16px; font-weight: 700; }
    .tx-dialog-form {
      display: flex; flex-direction: column; gap: 0;
      .full-width { width: 100%; }
      .mat-mdc-form-field { width: 100%; }
    }
  `],
})
export class TransactionDialogComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  account: Account;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<TransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TransactionDialogData,
    private transactionService: TransactionService,
    private snackBar: MatSnackBar,
  ) {
    this.account = data.account;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      transactionType: ['DEPOSIT', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(255)],
    });
  }

  getError(field: string): string {
    const c = this.form.get(field);
    if (!c?.errors || !c.touched) return '';
    if (c.errors['required']) return 'Campo obligatorio';
    if (c.errors['min']) return 'El monto debe ser mayor a $0';
    if (c.errors['maxlength']) return 'Máximo 255 caracteres';
    return 'Valor no válido';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', minimumFractionDigits: 2,
    }).format(value);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const { transactionType, amount, description } = this.form.value;
    this.transactionService
      .register(this.account.id, {
        transactionType,
        amount: parseFloat(amount),
        description: description || undefined,
      })
      .subscribe({
        next: (res) => {
          this.snackBar.open(res.message, 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err: Error) => {
          this.loading = false;
          this.snackBar.open(err.message, 'Cerrar', { duration: 5000, panelClass: 'snack-error' });
        },
      });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
