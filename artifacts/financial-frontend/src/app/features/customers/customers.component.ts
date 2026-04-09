import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerService } from '../../core/services/customer.service';
import { Customer } from '../../core/models/customer.model';
import { CustomerFormDialogComponent } from './customer-form-dialog/customer-form-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatTableModule, MatInputModule, MatFormFieldModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressSpinnerModule, MatCardModule,
  ],
  templateUrl: './customers.component.html',
})
export class CustomersComponent implements OnInit {
  displayedColumns = ['id', 'name', 'identification', 'email', 'birthDate', 'age', 'actions'];
  dataSource = new MatTableDataSource<Customer>([]);
  loading = true;
  error = '';

  constructor(
    private customerService: CustomerService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.error = '';
    this.customerService.getAll().subscribe({
      next: (customers) => {
        this.dataSource.data = customers;
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  openCreate(): void {
    const ref = this.dialog.open(CustomerFormDialogComponent, {
      data: {},
      width: '540px',
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadCustomers();
    });
  }

  openEdit(customer: Customer): void {
    const ref = this.dialog.open(CustomerFormDialogComponent, {
      data: { customer },
      width: '540px',
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadCustomers();
    });
  }

  confirmDelete(customer: Customer): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar cliente',
        message: `¿Está seguro de eliminar a <strong>${customer.firstName} ${customer.lastName}</strong>?<br>
          Esta acción no se puede deshacer. El cliente no puede tener cuentas activas.`,
        confirmLabel: 'Eliminar',
        cancelLabel: 'Cancelar',
      },
      width: '400px',
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.customerService.delete(customer.id).subscribe({
        next: () => {
          this.snackBar.open('Cliente eliminado exitosamente', 'Cerrar', { duration: 3000 });
          this.loadCustomers();
        },
        error: (err: Error) => {
          this.snackBar.open(err.message, 'Cerrar', { duration: 5000, panelClass: 'snack-error' });
        },
      });
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

  formatDate(dateStr?: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit', month: 'short', year: 'numeric',
    });
  }

  get totalCustomers(): number {
    return this.dataSource.data.length;
  }

  idTypeLabels: { [key: string]: string | undefined } = {
    CC: 'CC', CE: 'CE', TI: 'TI', PASSPORT: 'Pasaporte', NIT: 'NIT',
  };
}
