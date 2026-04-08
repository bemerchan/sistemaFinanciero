import { apiFetch } from './api';
import type { Account, AccountRequest, ApiResponse } from '@/types';

interface HalAccounts {
  _embedded?: { accounts: Account[] };
}

export async function getAccountsByCustomer(customerId: number): Promise<Account[]> {
  const res = await apiFetch<HalAccounts>(`/accounts/search/byCustomer?customerId=${customerId}`);
  return res._embedded?.accounts ?? [];
}

export async function createAccount(data: AccountRequest): Promise<ApiResponse<Account>> {
  return apiFetch<ApiResponse<Account>>('/accounts', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function getAccountBalance(id: number): Promise<ApiResponse<Account>> {
  return apiFetch<ApiResponse<Account>>(`/accounts/${id}/balance`);
}

export async function deleteAccount(id: number): Promise<void> {
  return apiFetch<void>(`/accounts/${id}`, { method: 'DELETE' });
}
