import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AccountType } from '../../../core/models/account.model';

@Component({
  selector: 'app-account-form-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Nueva cuenta</h2>

    <mat-dialog-content class="account-dialog-content">
      <p class="account-dialog-subtitle">Selecciona el tipo de cuenta que deseas crear</p>

      <div class="account-type-options">
        <div class="account-type-card"
             [class.selected]="selected === 'SAVINGS'"
             (click)="selected = 'SAVINGS'">
          <div class="account-type-icon savings">
            <mat-icon>savings</mat-icon>
          </div>
          <div class="account-type-info">
            <div class="account-type-title">Cuenta de Ahorro</div>
            <div class="account-type-desc">Número inicia con 53 · Saldo mínimo $0</div>
          </div>
          @if (selected === 'SAVINGS') {
            <mat-icon class="account-type-check">check_circle</mat-icon>
          }
        </div>

        <div class="account-type-card"
             [class.selected]="selected === 'CHECKING'"
             (click)="selected = 'CHECKING'">
          <div class="account-type-icon checking">
            <mat-icon>account_balance_wallet</mat-icon>
          </div>
          <div class="account-type-info">
            <div class="account-type-title">Cuenta Corriente</div>
            <div class="account-type-desc">Número inicia con 33 · Permite sobregiro</div>
          </div>
          @if (selected === 'CHECKING') {
            <mat-icon class="account-type-check">check_circle</mat-icon>
          }
        </div>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-flat-button color="primary" (click)="onSave()">
        <mat-icon>add</mat-icon>
        Crear cuenta
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .account-dialog-content {
      padding-top: 8px !important;
      overflow: visible !important;
      min-width: 380px;
    }
    .account-dialog-subtitle {
      color: #64748b;
      font-size: 14px;
      margin: 0 0 20px;
    }
    .account-type-options {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .account-type-card {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 16px;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      cursor: pointer;
      transition: border-color 0.15s, background 0.15s;
      background: white;
      &:hover { border-color: #93c5fd; background: #f8fafc; }
      &.selected { border-color: #3b82f6; background: #eff6ff; }
    }
    .account-type-icon {
      width: 42px; height: 42px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      &.savings  { background: #dbeafe; mat-icon { color: #3b82f6; } }
      &.checking { background: #dcfce7; mat-icon { color: #16a34a; } }
      mat-icon { font-size: 22px; width: 22px; height: 22px; }
    }
    .account-type-info { flex: 1; }
    .account-type-title { font-size: 14px; font-weight: 600; color: #1e293b; }
    .account-type-desc  { font-size: 12px; color: #64748b; margin-top: 2px; }
    .account-type-check { color: #3b82f6; font-size: 20px; width: 20px; height: 20px; }
  `],
})
export class AccountFormDialogComponent {
  selected: AccountType = 'SAVINGS';

  constructor(private dialogRef: MatDialogRef<AccountFormDialogComponent>) {}

  onSave(): void {
    this.dialogRef.close(this.selected);
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
