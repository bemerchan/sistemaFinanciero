import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TransactionService } from '../../../core/services/transaction.service';
import { Transaction } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './transaction-list.component.html',
})
export class TransactionListComponent implements OnChanges {
  @Input() accountId!: number;
  @Input() refreshTrigger = 0;

  transactions: Transaction[] = [];
  loading = false;
  error = '';

  constructor(private transactionService: TransactionService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accountId'] || changes['refreshTrigger']) {
      this.load();
    }
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.transactionService.getLast5(this.accountId).subscribe({
      next: (txs) => {
        this.transactions = txs;
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', minimumFractionDigits: 2,
    }).format(value);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('es-CO', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
