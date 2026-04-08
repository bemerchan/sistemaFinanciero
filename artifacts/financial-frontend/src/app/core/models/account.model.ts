export type AccountType = 'SAVINGS' | 'CHECKING';
export type AccountStatus = 'ACTIVE' | 'INACTIVE';

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

export interface AccountRequest {
  accountType: AccountType;
  customerId: number;
}

export interface HalAccounts {
  _embedded?: { accounts: Account[] };
}
