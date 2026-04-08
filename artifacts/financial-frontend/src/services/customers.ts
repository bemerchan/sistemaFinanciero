import { apiFetch } from './api';
import type { Customer, CustomerRequest, ApiResponse } from '@/types';

interface HalCustomers {
  _embedded: { customers: Customer[] };
  page: { size: number; totalElements: number; totalPages: number; number: number };
}

export async function getCustomers(): Promise<Customer[]> {
  const res = await apiFetch<HalCustomers>('/customers?size=200&sort=id,desc');
  return res._embedded?.customers ?? [];
}

export async function getCustomer(id: number): Promise<Customer> {
  return apiFetch<Customer>(`/customers/${id}`);
}

export async function createCustomer(data: CustomerRequest): Promise<ApiResponse<Customer>> {
  return apiFetch<ApiResponse<Customer>>('/customers', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateCustomer(id: number, data: CustomerRequest): Promise<ApiResponse<Customer>> {
  return apiFetch<ApiResponse<Customer>>(`/customers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function deleteCustomer(id: number): Promise<void> {
  return apiFetch<void>(`/customers/${id}`, { method: 'DELETE' });
}
