import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Account } from '../../../core/models/account.model';
import { AccountService } from '../../../core/services/account.service';
import { TransactionFormComponent } from '../transaction-form/transaction-form.component';
import { TransactionListComponent } from '../transaction-list/transaction-list.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-account-card',
  standalone: true,
  imports: [
    CommonModule, MatExpansionModule, MatIconModule, MatButtonModule,
    TransactionFormComponent, TransactionListComponent,
  ],
  templateUrl: './account-card.component.html',
})
export class AccountCardComponent {
  @Input() account!: Account;
  @Input() customerId!: number;
  @Output() accountDeleted = new EventEmitter<void>();
  @Output() balanceUpdated = new EventEmitter<void>();

  refreshTrigger = 0;

  constructor(
    private accountService: AccountService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  get isActive(): boolean {
    return this.account.status === 'ACTIVE';
  }

  get typeClass(): string {
    return this.account.accountType === 'SAVINGS' ? 'savings' : 'checking';
  }

  get typeLabel(): string {
    return this.account.accountType === 'SAVINGS' ? 'Ahorro' : 'Corriente';
  }

  get typeIcon(): string {
    return this.account.accountType === 'SAVINGS' ? 'savings' : 'account_balance_wallet';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', minimumFractionDigits: 2,
    }).format(value);
  }

  onTransactionRegistered(): void {
    this.refreshTrigger++;
    this.balanceUpdated.emit();
  }

  confirmDelete(event: MouseEvent): void {
    event.stopPropagation();
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar cuenta',
        message: `¿Eliminar la cuenta <strong>${this.account.accountNumber}</strong>?<br>
          Saldo actual: <strong>${this.formatCurrency(this.account.balance)}</strong>.<br>
          Esta acción no se puede deshacer.`,
        confirmLabel: 'Eliminar',
      },
      width: '400px',
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.accountService.delete(this.account.id).subscribe({
        next: () => {
          this.snackBar.open('Cuenta eliminada exitosamente', 'Cerrar', { duration: 3000 });
          this.accountDeleted.emit();
        },
        error: (err: Error) => {
          this.snackBar.open(err.message, 'Cerrar', { duration: 5000, panelClass: 'snack-error' });
        },
      });
    });
  }
}
