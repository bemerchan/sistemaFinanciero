import { apiFetch } from './api';
import type { Transaction, TransactionRequest, ApiResponse } from '@/types';

interface HalTransactions {
  _embedded?: { transactions: Transaction[] };
  page?: { size: number; totalElements: number; totalPages: number; number: number };
}

export async function getLastTransactions(accountId: number, size = 5): Promise<Transaction[]> {
  const res = await apiFetch<HalTransactions>(
    `/transactions/search/byAccount?accountId=${accountId}&page=0&size=${size}`
  );
  return res._embedded?.transactions ?? [];
}

export async function registerTransaction(
  accountId: number,
  data: TransactionRequest
): Promise<ApiResponse<Transaction>> {
  return apiFetch<ApiResponse<Transaction>>(`/transactions/account/${accountId}`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
