import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer, CustomerRequest } from '../../../core/models/customer.model';

export interface CustomerFormDialogData {
  customer?: Customer;
}

@Component({
  selector: 'app-customer-form-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  templateUrl: './customer-form-dialog.component.html',
})
export class CustomerFormDialogComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  isEdit: boolean;

  idTypes = [
    { value: 'CC', label: 'CC - Cédula de Ciudadanía' },
    { value: 'CE', label: 'CE - Cédula de Extranjería' },
    { value: 'TI', label: 'TI - Tarjeta de Identidad' },
    { value: 'PASSPORT', label: 'Pasaporte' },
    { value: 'NIT', label: 'NIT' },
  ];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<CustomerFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CustomerFormDialogData,
    private customerService: CustomerService,
    private snackBar: MatSnackBar
  ) {
    this.isEdit = !!data?.customer;
  }

  ngOnInit(): void {
    const c = this.data?.customer;
    this.form = this.fb.group({
      firstName: [c?.firstName ?? '', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: [c?.lastName ?? '', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      identificationType: [c?.identificationType ?? 'CC', Validators.required],
      identificationNumber: [c?.identificationNumber ?? '', [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
      email: [c?.email ?? '', [Validators.required, Validators.email, Validators.maxLength(150)]],
      birthDate: [c?.birthDate ?? '', Validators.required],
    });
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (!control?.errors || !control.touched) return '';
    if (control.errors['required']) return 'Campo obligatorio';
    if (control.errors['email']) return 'Correo no válido';
    if (control.errors['minlength']) return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    if (control.errors['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
    return 'Valor no válido';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const data: CustomerRequest = this.form.value;
    const op$ = this.isEdit
      ? this.customerService.update(this.data.customer!.id, data)
      : this.customerService.create(data);

    op$.subscribe({
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
