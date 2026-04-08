import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { TransactionService } from '../../../core/services/transaction.service';
import { Transaction } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule, MatPaginatorModule],
  templateUrl: './transaction-list.component.html',
})
export class TransactionListComponent implements OnChanges {
  @Input() accountId!: number;
  @Input() refreshTrigger = 0;

  @ViewChild(MatPaginator) paginator?: MatPaginator;

  transactions: Transaction[] = [];
  loading = false;
  error = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;

  constructor(private transactionService: TransactionService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accountId']) {
      this.currentPage = 0;
      this.paginator?.firstPage();
    }
    if (changes['accountId'] || changes['refreshTrigger']) {
      this.load();
    }
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.transactionService.getByAccount(this.accountId, this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        this.transactions = res._embedded?.transactions ?? [];
        this.totalElements = res.page?.totalElements ?? 0;
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
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
