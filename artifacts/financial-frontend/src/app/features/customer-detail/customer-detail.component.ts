import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CustomerService } from '../../core/services/customer.service';
import { AccountService } from '../../core/services/account.service';
import { Customer } from '../../core/models/customer.model';
import { Account, AccountType } from '../../core/models/account.model';
import { AccountCardComponent } from './account-card/account-card.component';
import { AccountFormDialogComponent } from './account-form-dialog/account-form-dialog.component';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, AccountCardComponent,
  ],
  templateUrl: './customer-detail.component.html',
})
export class CustomerDetailComponent implements OnInit {
  customer?: Customer;
  accounts: Account[] = [];
  loading = true;
  error = '';
  customerId!: number;

  newAccountLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: CustomerService,
    private accountService: AccountService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.customerId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      customer: this.customerService.getById(this.customerId),
      accounts: this.accountService.getByCustomer(this.customerId),
    }).subscribe({
      next: ({ customer, accounts }) => {
        this.customer = customer;
        this.accounts = accounts;
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  reloadAccounts(): void {
    this.accountService.getByCustomer(this.customerId).subscribe({
      next: (accounts) => (this.accounts = accounts),
    });
  }

  reloadCustomerAndAccounts(): void {
    forkJoin({
      customer: this.customerService.getById(this.customerId),
      accounts: this.accountService.getByCustomer(this.customerId),
    }).subscribe({
      next: ({ customer, accounts }) => {
        this.customer = customer;
        this.accounts = accounts;
      },
    });
  }

  openAddAccount(): void {
    const ref = this.dialog.open(AccountFormDialogComponent, {
      width: '440px',
    });
    ref.afterClosed().subscribe((accountType: AccountType | null) => {
      if (!accountType) return;
      this.newAccountLoading = true;
      this.accountService.create({ accountType, customerId: this.customerId }).subscribe({
        next: (res) => {
          this.newAccountLoading = false;
          this.snackBar.open(res.message, 'Cerrar', { duration: 3000 });
          this.reloadAccounts();
        },
        error: (err: Error) => {
          this.newAccountLoading = false;
          this.snackBar.open(err.message, 'Cerrar', { duration: 5000, panelClass: 'snack-error' });
        },
      });
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  get initials(): string {
    if (!this.customer) return '?';
    return `${this.customer.firstName[0]}${this.customer.lastName[0]}`.toUpperCase();
  }

  get totalBalance(): number {
    return this.accounts.reduce((sum, a) => sum + a.balance, 0);
  }

  get activeAccounts(): number {
    return this.accounts.filter(a => a.status === 'ACTIVE').length;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', minimumFractionDigits: 2,
    }).format(value);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit', month: 'long', year: 'numeric',
    });
  }

  calcAge(birthDate: string): number {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
  }

  idTypeLabels: { [key: string]: string | undefined } = {
    CC: 'Cédula de Ciudadanía',
    CE: 'Cédula de Extranjería',
    TI: 'Tarjeta de Identidad',
    PASSPORT: 'Pasaporte',
    NIT: 'NIT',
  };
}
