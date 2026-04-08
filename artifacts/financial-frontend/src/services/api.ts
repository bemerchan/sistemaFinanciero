import type { ApiErrorBody } from '@/types';

export class ApiError extends Error {
  status: number;
  errors?: ApiErrorBody['errors'];

  constructor(status: number, message: string, errors?: ApiErrorBody['errors']) {
    super(message);
    this.status = status;
    this.errors = errors;
  }
}

const API_BASE = '/api/v1';

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (res.status === 204) {
    return null as T;
  }

  const json = await res.json().catch(() => ({}));

  if (!res.ok) {
    const body = json as ApiErrorBody;
    throw new ApiError(res.status, body.message || 'Error en la solicitud', body.errors);
  }

  return json as T;
}
