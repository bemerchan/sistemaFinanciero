export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL';

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

export interface TransactionRequest {
  transactionType: TransactionType;
  amount: number;
  description?: string;
}

export interface HalTransactions {
  _embedded?: { transactions: Transaction[] };
  page?: { size: number; totalElements: number; totalPages: number; number: number };
}
