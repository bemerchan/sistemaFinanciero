export type IdentificationType = 'CC' | 'CE' | 'TI' | 'PASSPORT' | 'NIT';
export type AccountType = 'SAVINGS' | 'CHECKING';
export type AccountStatus = 'ACTIVE' | 'INACTIVE';
export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  identificationType: IdentificationType;
  identificationNumber: string;
  email: string;
  birthDate: string;
  age?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Account {
  id: number;
  accountNumber: string;
  accountType: AccountType;
  status: AccountStatus;
  balance: number;
  customerId: number;
  customerFullName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Transaction {
  id: number;
  transactionType: TransactionType;
  amount: number;
  balanceAfter: number;
  description?: string;
  accountId: number;
  accountNumber?: string;
  createdAt?: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  identificationType: IdentificationType;
  identificationNumber: string;
  email: string;
  birthDate: string;
}

export interface AccountRequest {
  accountType: AccountType;
  customerId: number;
}

export interface TransactionRequest {
  transactionType: TransactionType;
  amount: number;
  description?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ErrorField {
  field?: string;
  rejectedValue?: string;
  message: string;
}

export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  errors?: ErrorField[];
  timestamp: string;
}
