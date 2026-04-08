import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TransactionService } from '../../../core/services/transaction.service';
import { Account } from '../../../core/models/account.model';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule, MatIconModule,
  ],
  templateUrl: './transaction-form.component.html',
})
export class TransactionFormComponent implements OnInit {
  @Input() account!: Account;
  @Input() customerId!: number;
  @Output() transactionRegistered = new EventEmitter<void>();

  form!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      transactionType: ['DEPOSIT', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(255)],
    });
    if (!this.isActive) {
      this.form.disable();
    }
  }

  get isActive(): boolean {
    return this.account.status === 'ACTIVE';
  }

  getError(field: string): string {
    const c = this.form.get(field);
    if (!c?.errors || !c.touched) return '';
    if (c.errors['required']) return 'Campo obligatorio';
    if (c.errors['min']) return 'Mínimo $0.01';
    if (c.errors['maxlength']) return 'Máximo 255 caracteres';
    return 'Valor no válido';
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
          this.loading = false;
          this.form.reset({ transactionType: 'DEPOSIT', amount: '', description: '' });
          this.transactionRegistered.emit();
        },
        error: (err: Error) => {
          this.loading = false;
          this.snackBar.open(err.message, 'Cerrar', { duration: 5000, panelClass: 'snack-error' });
        },
      });
  }
}
